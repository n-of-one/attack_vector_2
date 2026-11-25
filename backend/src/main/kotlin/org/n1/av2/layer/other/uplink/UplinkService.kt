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
import org.n1.av2.run.RunIngressService
import org.n1.av2.run.RunService
import org.n1.av2.run.UplinkConnectInfo
import org.n1.av2.run.connectionRefusalReason
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.runlink.RunLinkEntityService
import org.n1.av2.run.runlink.RunLinkService
import org.n1.av2.run.terminal.SyntaxHighlightingService
import org.n1.av2.run.terminal.TERMINAL_MAIN
import org.n1.av2.run.terminal.inside.CommandMoveService
import org.n1.av2.run.timings.TimingsService
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.NodeRepo
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

private const val NO_CARRIER = "[warn]No carrier.[/] The uplink target does not respond."

@Service
class UplinkService(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val runEntityService: RunEntityService,
    private val runService: RunService,
    private val runIngressService: RunIngressService,
    private val runLinkEntityService: RunLinkEntityService,
    private val runLinkService: RunLinkService,
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
        val targetSiteId = layer.targetSiteId ?: return connectionService.replyTerminalReceive(NO_CARRIER)
        val target = sitePropertiesEntityService.getBySiteId(targetSiteId)
        val refusal = connectionRefusalReason(target)
        if (refusal != null) {
            connectionService.replyTerminalReceive(refusal)
            return
        }
        val ingressNetworkId = layer.targetNodeNetworkId ?: target.startNodeNetworkId
        val ingressNode = nodeEntityService.findByNetworkId(target.siteId, ingressNetworkId) ?: return connectionService.replyTerminalReceive(NO_CARRIER)

        val sourceRun = runEntityService.getByRunId(hackerState.runId)
        val targetRun = findOrCreateTargetRun(sourceRun, target, hackerState.userId)
        addUserRunLink(hackerState, targetRun)
        runLinkService.sendRunInfosToUser(hackerState.userId) // the run on the target can be new to this hacker, it receives run info updates from now on

        val uplinkNodeId = hackerState.currentNodeId ?: error("Hacker inside without node: ${hackerState.userId}")

        connectionService.replyTerminalReceiveAndLocked(true, "Establishing uplink carrier...")

        val timings = timingsService.skillAndConfigAdjusted(timingsService.START_UPLINK, hackerState.userId)
        val userId = hackerState.userId
        userTaskRunner.queueInTicksForSite("uplink-establish", hackerState.siteId, timings.ticksFor("establish")) {
            carrierEstablished(userId, sourceRun.runId, uplinkNodeId, targetRun.runId, ingressNode.id)
        }
    }

    private fun addUserRunLink(hackerState: HackerStateRunning,targetRun: Run) {
        if (!runLinkEntityService.hasUserRunLink(userEntityService.getById(hackerState.userId), targetRun.runId)) {
            runLinkEntityService.createRunLink(targetRun.runId, userEntityService.getById(hackerState.userId))
        }
    }

    @ScheduledTask
    fun carrierEstablished(userId: String, sourceRunId: String, uplinkNodeId: String, targetRunId: String, ingressNodeId: String) {
        val hackerState = hackerStateEntityService.retrieveForUserId(userId)
        if (hackerState.runId != sourceRunId || hackerState.currentNodeId != uplinkNodeId) return // hacker was disconnected in the meantime

        val targetRun = runEntityService.getByRunId(targetRunId)
        val target = sitePropertiesEntityService.getBySiteId(targetRun.siteId)
        val refusal = connectionRefusalReason(target)
        if (refusal != null) {
            connectionService.replyTerminalReceiveAndLocked(false, refusal)
            return
        }
        if (nodeRepo.findById(ingressNodeId).getOrNull()?.siteId != target.siteId) { // removed by a GM in the meantime
            connectionService.replyTerminalReceiveAndLocked(false, NO_CARRIER)
            return
        }

        val sourceIngressNodeId = hackerState.ingressNodeId ?: error("Hacker ${userId} inside without ingress node")
        val uplinkReturns = hackerState.uplinkReturns + UplinkReturn(sourceRunId, hackerState.siteId!!, uplinkNodeId, sourceIngressNodeId)

        runService.revealNode(targetRun, ingressNodeId)
        runService.leaveSite(hackerState, false, viaUplink = true)
        hackerStateEntityService.prepareUplinkConnect(hackerState, targetRun, ingressNodeId, uplinkReturns)
        sendEnteringRun(hackerState.userId, targetRun, uplinkReturn = false)
        // The browser now calls /run/enterSiteFromUplink
    }

    // Hackers that share the source run share the run on the target. The first hacker continues their own run on the target if they have one.
    private fun findOrCreateTargetRun(sourceRun: Run, target: SiteProperties, userId: String): Run {
        val run = sourceRun.uplinkRunIds[target.siteId]?.let { runEntityService.findByRunId(it) }
            ?: runService.findRunOfUserForSite(userId, target.siteId)
            ?: runService.startNewRun(target, userEntityService.getById(userId), sourceRun.runId)

        if (sourceRun.uplinkRunIds[target.siteId] != run.runId) {
            sourceRun.uplinkRunIds[target.siteId] = run.runId
            runEntityService.save(sourceRun)
        }
        return run
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
                val enteredState = hackerStateEntityService.updateConnectionId(hackerState, connectionId)
                runService.sendEnteredRun(userId, run, enteredState)
                startConnectionSequence(enteredState)
            }
            UplinkActivity.RETURNING -> {
                sendInsidePrompt()
                val enteredState = hackerStateEntityService.enterRunInsideBackFromUplinkedSite(hackerState, connectionId)
                runService.sendEnteredRun(userId, run, enteredState)
                waitForSiteToRender(enteredState.toRunState())
            }
            UplinkActivity.RETURNING_OUTSIDE -> {
                connectionService.reply(ServerActions.SERVER_TERMINAL_UPDATE_PROMPT, "prompt" to "⇀ ", "terminalId" to TERMINAL_MAIN)
                runService.enterRunOutside(userId, run, connectionId)
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

    private fun startConnectionSequence(hackerState: HackerState) {
        val sourceSiteName = sitePropertiesEntityService.getBySiteId(hackerState.uplinkReturns.last().siteId).name
        val targetSiteName = sitePropertiesEntityService.getBySiteId(hackerState.siteId!!).name
        val ingressNodeId = hackerState.ingressNodeId ?: error("Hacker ${hackerState.userId} connects via uplink without ingress node")
        runIngressService.connect(hackerState, ingressNodeId, quick = false, UplinkConnectInfo(sourceSiteName, targetSiteName))
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

            if (canReturnToNode(site, uplinkReturn)) {
                hackerStateEntityService.prepareUplinkReturn(hackerState, run, uplinkReturn, remaining)
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

    // Same rule as for the hackers that stayed in the site: the uplink node must still be reachable from where the hacker connected to it.
    private fun canReturnToNode(site: SiteProperties, uplinkReturn: UplinkReturn): Boolean {
        if (site.shutdownEnd != null) return false
        val node = nodeRepo.findById(uplinkReturn.nodeId).getOrNull() ?: return false
        if (node.layers.none { it is UplinkLayer }) return false
        return networkSwitchService.isReachable(site.siteId, uplinkReturn.ingressNodeId, uplinkReturn.nodeId, pendingIsOpen = true)
    }

    private fun sendTerminal(userId: String, vararg lines: String, locked: Boolean = true) {
        connectionService.toUser(userId, ServerActions.SERVER_TERMINAL_RECEIVE, ConnectionService.TerminalReceive(TERMINAL_MAIN, lines, locked))
    }
}
