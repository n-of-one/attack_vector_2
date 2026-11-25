package org.n1.av2.layer.other.uplink

import io.github.oshai.kotlinlogging.KotlinLogging
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.hacker.hackerstate.UplinkActivity
import org.n1.av2.hacker.hackerstate.UplinkReturn
import org.n1.av2.layer.other.networkswitch.NetworkSwitchService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.CalledBySystem
import org.n1.av2.platform.engine.ScheduledTask
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.run.RunService
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.runlink.RunLinkEntityService
import org.n1.av2.run.terminal.SyntaxHighlightingService
import org.n1.av2.run.terminal.TERMINAL_MAIN
import org.n1.av2.run.terminal.inside.CommandMoveService
import org.n1.av2.run.timings.Timings
import org.n1.av2.run.timings.TimingsService
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.NodeRepo
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class UplinkService(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val runEntityService: RunEntityService,
    private val runService: RunService,
    private val runLinkEntityService: RunLinkEntityService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val userEntityService: UserEntityService,
    private val nodeEntityService: NodeEntityService,
    private val nodeRepo: NodeRepo,
    private val networkSwitchService: NetworkSwitchService,
    private val commandMoveService: CommandMoveService,
    private val connectionService: ConnectionService,
    private val userTaskRunner: UserTaskRunner,
    private val timingsService: TimingsService,
    private val syntaxHighlightingService: SyntaxHighlightingService,
) {

    private val logger = KotlinLogging.logger {}

    fun hack(layer: UplinkLayer, hackerState: HackerStateRunning) {
        val targetSiteId = layer.targetSiteId
        if (targetSiteId == null) {
            connectionService.replyTerminalReceive("[warn]No carrier.[/] The uplink target does not respond.")
            return
        }
        val target = sitePropertiesEntityService.getBySiteId(targetSiteId)
        val refusal = refusalReason(target)
        if (refusal != null) {
            connectionService.replyTerminalReceive(refusal)
            return
        }

        val sourceRun = runEntityService.getByRunId(hackerState.runId)
        val targetRun = findOrCreateTargetRun(sourceRun, target, hackerState.userId)
        addUserRunLink(hackerState, targetRun)

        val uplinkNodeId = hackerState.currentNodeId ?: error("Hacker inside without node: ${hackerState.userId}")

        connectionService.replyTerminalReceiveAndLocked(true, "Establishing uplink carrier...")

        val timings = timingsService.skillAndConfigAdjusted(timingsService.START_UPLINK, hackerState.userId)
        val userId = hackerState.userId
        userTaskRunner.queueInTicksForSite("uplink-establish", hackerState.siteId, timings.ticksFor("establish")) {
            carrierEstablished(userId, sourceRun.runId, uplinkNodeId, targetRun.runId)
        }
    }

    private fun addUserRunLink(hackerState: HackerStateRunning,targetRun: Run) {
        if (!runLinkEntityService.hasUserRunLink(userEntityService.getById(hackerState.userId), targetRun.runId)) {
            runLinkEntityService.createRunLink(targetRun.runId, userEntityService.getById(hackerState.userId))
        }
    }

    @ScheduledTask
    fun carrierEstablished(userId: String, sourceRunId: String, uplinkNodeId: String, targetRunId: String) {
        val hackerState = hackerStateEntityService.retrieveForUserId(userId)
        if (hackerState.runId != sourceRunId || hackerState.currentNodeId != uplinkNodeId) return // hacker was disconnected in the meantime

        val targetRun = runEntityService.getByRunId(targetRunId)
        val target = sitePropertiesEntityService.getBySiteId(targetRun.siteId)
        val refusal = refusalReason(target)
        if (refusal != null) {
            connectionService.replyTerminalReceiveAndLocked(false, refusal)
            return
        }
        val startNode = nodeEntityService.findByNetworkId(target.siteId, target.startNodeNetworkId) ?: error("Start node not found for ${target.name}")

        val uplinkReturns = hackerState.uplinkReturns + UplinkReturn(sourceRunId, hackerState.siteId!!, uplinkNodeId)
        switchRun(hackerState, targetRun, startNode.id, uplinkReturns)
    }

    private fun refusalReason(target: SiteProperties): String? {
        if (!target.hackable || !target.siteStructureOk) return "[warn]Connection refused.[/] [info]${target.name}[/] is not accepting connections."
        if (target.shutdownEnd != null) return "[warn]Connection refused.[/] [info]${target.name}[/] is shut down."
        return null
    }

    private fun findOrCreateTargetRun(sourceRun: Run, target: SiteProperties, userId: String): Run {
        val existing = sourceRun.uplinkRunIds[target.siteId]?.let { runEntityService.findByRunId(it) }
        if (existing != null) return existing

        val run = runService.startNewRun(target, userEntityService.getById(userId), sourceRun.runId)
        sourceRun.uplinkRunIds[target.siteId] = run.runId
        runEntityService.save(sourceRun)
        return run
    }

    private fun switchRun(hackerState: HackerState, targetRun: Run, arrivalNodeId: String, uplinkReturns: List<UplinkReturn>) {
        runService.leaveSite(hackerState, false, viaUplink = true)
        hackerStateEntityService.prepareUplinkArrival(hackerState, targetRun, arrivalNodeId, uplinkReturns, UplinkActivity.CONNECTING)
        sendEnteringRun(hackerState.userId, targetRun, uplinkReturn = false)
        // The browser now calls /run/enterSiteFromUplink
    }

    // The browser answers with /run/enterSiteFromUplink, see enterSiteFromUplink
    private fun sendEnteringRun(userId: String, run: Run, uplinkReturn: Boolean = false) {
        connectionService.toUser(
            userId, ServerActions.SERVER_ENTERING_RUN,
            "runId" to run.runId, "siteId" to run.siteId, "viaUplink" to true, "uplinkReturn" to uplinkReturn
        )
    }

    fun enterSiteFromUplink(userId: String, runId: String, connectionId: String) {
        val hackerState = hackerStateEntityService.retrieveForUserId(userId)
        val run = runEntityService.getByRunId(runId)

        when (hackerState.uplinkActivity) {
            UplinkActivity.CONNECTING -> {
                sendInsidePrompt()
                val enteredState = hackerStateEntityService.enterRunViaUplinkOutsideWhileConnecting(hackerState, connectionId)
                runService.sendEnteredRun(userId, run, enteredState)
                startConnectionSequence(enteredState.toRunState())
            }
            UplinkActivity.RETURNING -> {
                sendInsidePrompt()
                val enteredState = hackerStateEntityService.enterRunInsideBackFromUplinkedSite(hackerState, connectionId)
                runService.sendEnteredRun(userId, run, enteredState)
                waitForSiteToRender(enteredState.toRunState())
            }
            UplinkActivity.RETURNING_OUTSIDE -> {
                syntaxHighlightingService.sendForOutside()
                connectionService.reply(ServerActions.SERVER_TERMINAL_UPDATE_PROMPT, "prompt" to "⇀ ", "terminalId" to TERMINAL_MAIN)
                val enteredState = hackerStateEntityService.enterRunOutside(run.siteId, userId, runId, connectionId)
                runService.sendEnteredRun(userId, run, enteredState)
            }
            UplinkActivity.NONE -> {
                // the browser waits before entering, in the meantime the hacker left the run (or something else changed their state)
                return
            }
        }
    }

    private fun sendInsidePrompt() {
        syntaxHighlightingService.sendForInside()
        connectionService.reply(ServerActions.SERVER_TERMINAL_UPDATE_PROMPT, "prompt" to "⇋ ", "terminalId" to TERMINAL_MAIN)
    }

    private fun waitForSiteToRender(hackerState: HackerStateRunning) {
        sendTerminal(hackerState.userId, "Upgrading connection to persona matrix...")
        val userId = hackerState.userId
        val runId = hackerState.runId
        val nodeId = hackerState.currentNodeId!!
        userTaskRunner.queueInTicksForSite("uplink-return", hackerState.siteId, timingsService.UPLINK_RETURN.ticksFor("main")) {
            sessionRestored(userId, runId, nodeId)
        }
    }

    @ScheduledTask
    fun sessionRestored(userId: String, runId: String, nodeId: String) {
        val hackerState = hackerStateEntityService.retrieveForUserId(userId)
        if (hackerState.runId != runId || hackerState.currentNodeId != nodeId) return // hacker was disconnected in the meantime

        val node = nodeEntityService.getById(nodeId)
        val siteName = sitePropertiesEntityService.getBySiteId(hackerState.siteId!!).name
        sendTerminal(userId, "Session restored at node [ok]${node.networkId}[/] of [info]${siteName}", "")
        commandMoveService.moveArrive(nodeId, userId, runId)
    }

    private fun startConnectionSequence(hackerState: HackerStateRunning) {
        val sourceSiteName = sitePropertiesEntityService.getBySiteId(hackerState.uplinkReturns.last().siteId).name
        val targetSiteName = sitePropertiesEntityService.getBySiteId(hackerState.siteId).name
        val timings = timingsService.skillAndConfigAdjusted(timingsService.START_UPLINK, hackerState.userId)

        class StartUplink(val userId: String, val sourceSiteName: String, val targetSiteName: String, val timings: Timings)
        connectionService.toRun(hackerState.runId, ServerActions.SERVER_HACKER_START_UPLINK, StartUplink(hackerState.userId, sourceSiteName, targetSiteName, timings))

        val userId = hackerState.userId
        val runId = hackerState.runId
        userTaskRunner.queueInTicksForSite("uplink-arrive", hackerState.siteId, timings.ticksFor("main")) { connectionSequenceFinished(userId, runId) }
    }

    @ScheduledTask
    fun connectionSequenceFinished(userId: String, runId: String) {
        val hackerState = hackerStateEntityService.retrieveForUserId(userId)
        if (hackerState.runId != runId || hackerState.uplinkActivity != UplinkActivity.CONNECTING) return // left the run during the sequence

        val target = sitePropertiesEntityService.getBySiteId(hackerState.siteId!!)
        val refusal = refusalReason(target)
        if (refusal != null) {
            returnFromUplink(hackerState, refusal)
            return
        }
        val arrivedState = hackerStateEntityService.arriveViaUplink(hackerState)
        commandMoveService.moveArrive(arrivedState.currentNodeId!!, userId, runId)
    }

    @CalledBySystem
    fun returnFromUplink(hackerState: HackerState, message: String) {
        val siteId = hackerState.siteId ?: error("Hacker ${hackerState.userId} returns via an uplink, but is not in a site")
        val siteName = sitePropertiesEntityService.getBySiteId(siteId).name
        runService.leaveSite(hackerState, false, viaUplink = true)
        sendTerminal(hackerState.userId, "[info]${message}", "Uplink to [info]${siteName}[/] dropped.")

        var remaining = hackerState.uplinkReturns
        while (remaining.isNotEmpty()) {
            val uplinkReturn = remaining.last()
            remaining = remaining.dropLast(1)

            val run = runEntityService.findByRunId(uplinkReturn.runId) ?: continue
            val site = sitePropertiesEntityService.findBySiteId(uplinkReturn.siteId) ?: continue
            if (!site.hackable) continue

            if (canReturnToNode(site, uplinkReturn.nodeId)) {
                hackerStateEntityService.prepareUplinkArrival(hackerState, run, uplinkReturn.nodeId, remaining, UplinkActivity.RETURNING)
                sendEnteringRun(hackerState.userId, run, uplinkReturn = true)
                return
            }
            if (!site.internal) {
                sendTerminal(hackerState.userId, "Session restored outside [info]${site.name}[/].", "", locked = false)
                hackerStateEntityService.prepareUplinkReturnOutside(hackerState, run)
                sendEnteringRun(hackerState.userId, run, uplinkReturn = true)
                return
            }
        }

        hackerStateEntityService.leaveSite(hackerState)
        sendTerminal(hackerState.userId, locked = false)
        connectionService.toUser(hackerState.userId, ServerActions.SERVER_RETURN_HOME)
    }

    private fun canReturnToNode(site: SiteProperties, nodeId: String): Boolean {
        if (site.shutdownEnd != null) return false
        val node = nodeRepo.findById(nodeId).getOrNull() ?: return false
        if (node.layers.none { it is UplinkLayer }) return false
        return networkSwitchService.isReachable(site.siteId, nodeId, pendingIsOpen = false)
    }

    private fun sendTerminal(userId: String, vararg lines: String, locked: Boolean = true) {
        connectionService.toUser(userId, ServerActions.SERVER_TERMINAL_RECEIVE, ConnectionService.TerminalReceive(TERMINAL_MAIN, lines, locked))
    }
}
