package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.CalledBySystem
import org.n1.av2.backend.engine.TaskEngine
import org.n1.av2.backend.engine.TaskIdentifiers
import org.n1.av2.backend.entity.run.*
import org.n1.av2.backend.entity.run.NodeScanStatus.*
import org.n1.av2.backend.entity.service.TimerEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.HackerIcon
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.entity.user.UserType
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.service.layerhacking.service.TimerInfo
import org.n1.av2.backend.service.layerhacking.service.TripwireLayerService
import org.n1.av2.backend.service.run.terminal.SyntaxHighlightingService
import org.n1.av2.backend.service.site.ScanInfoService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.util.isOneOf
import org.springframework.stereotype.Service

@Service
class RunService(
    private val runEntityService: RunEntityService,
    private val currentUserService: CurrentUserService,
    private val siteService: SiteService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val userEntityService: UserEntityService,
    private val taskEngine: TaskEngine,
    private val syntaxHighlightingService: SyntaxHighlightingService,
    private val stompService: StompService,
    private val userRunLinkEntityService: UserRunLinkEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val scanInfoService: ScanInfoService,
    private val timerEntityService: TimerEntityService,
    private val tripwireLayerService: TripwireLayerService,
) {

    class HackerPresence(
        val userId: String,
        val userName: String,
        val icon: HackerIcon,
        val nodeId: String?,
        val activity: HackerActivity,
        val masked: Boolean
    )

    class SiteInfo(val run: Run, val site: SiteFull, val hackers: List<HackerPresence>, val timers: List<TimerInfo>)

    fun startNewRun(siteName: String) {
        val siteProperties = sitePropertiesEntityService.findByName(siteName)
        if (siteProperties == null) {
            stompService.replyMessage(NotyMessage(NotyType.NEUTRAL, "Error", "Site '${siteName}' not found"))
            return
        }

        val nodeScanById = scanInfoService.createNodeScans(siteProperties.siteId)
        val run = runEntityService.create(siteProperties.siteId, nodeScanById, currentUserService.userId)
        userRunLinkEntityService.createUserScan(run.runId, currentUserService.userEntity)

        stompService.reply(ServerActions.SERVER_SITE_DISCOVERED, "runId" to run.runId, "siteId" to siteProperties.siteId)
        scanInfoService.sendScanInfosOfPlayer() // to update the scans in the home screen

        // enterRun will be called by the browser, the frontend needs to configure websocket connection for the run.
    }

    fun enterRun(runId: String) {
        val run = runEntityService.getByRunId(runId)
        val thisHackerState = hackerStateEntityService.enterSite(run.siteId, runId)

        syntaxHighlightingService.sendForOutside()
        stompService.toRun(runId, ServerActions.SERVER_HACKER_ENTER_SITE, toPresence(thisHackerState))

        val siteFull = siteService.getSiteFull(run.siteId)
        siteFull.sortNodeByDistance(run)

        val hackerPresences = getPresenceInRun(runId)

        val timers = tripwireLayerService.findForEnterSite(run.siteId, currentUserService.userId)

        val siteInfo = SiteInfo(run, siteFull, hackerPresences, timers)
        stompService.reply(ServerActions.SERVER_ENTER_RUN, siteInfo)
    }

    private fun getPresenceInRun(runId: String): List<HackerPresence> {
        val hackersInRun = hackerStateEntityService.getHackersInRun(runId)

        return hackersInRun.map { state -> toPresence(state) }
    }

    private fun toPresence(state: HackerState): HackerPresence {
        val user = userEntityService.getById(state.userId)
        if (user.type != UserType.HACKER || user.hacker == null) error("${user.name} is not a hacker")

        return HackerPresence(
            userId = user.id,
            userName = user.name,
            icon = user.hacker.icon,
            nodeId = state.currentNodeId,
            masked = state.masked,
            activity = state.activity
        )
    }

    @CalledBySystem
    fun hackerDisconnect(hackerState: HackerState, message: String) {
        taskEngine.removeForUser(hackerState.userId)

        stompService.toRun(hackerState.runId!!, ServerActions.SERVER_HACKER_DC, "userId" to hackerState.userId)
        stompService.toUser(
            hackerState.userId,
            ServerActions.SERVER_TERMINAL_RECEIVE,
            StompService.TerminalReceive(TERMINAL_MAIN, arrayOf("[info]${message}", ""))
        )
        stompService.toUser(hackerState.userId, ServerActions.SERVER_TERMINAL_UPDATE_PROMPT, "prompt" to "⇀ ", "terminalId" to TERMINAL_MAIN)

        hackerStateEntityService.disconnect(hackerState)
    }


    fun leaveSite(hackerState: HackerState) {
        val runId = hackerState.runId ?: return // if somehow the user was already disconnected for another reason

        taskEngine.removeForUser(hackerState.userId)

        class HackerLeaveNotification(val userId: String)
        stompService.toRun(runId, ServerActions.SERVER_HACKER_LEAVE_SITE, HackerLeaveNotification(hackerState.userId))

        hackerStateEntityService.leaveSite(hackerState)
    }


    fun updateNodeStatusToHacked(node: Node) {
        val runs = runEntityService.findAllForSiteId(node.siteId)

        val neighboringNodeIds = siteService.findNeighboringNodeIds(node)

        runs.forEach { run ->
            val currentStatus = run.nodeScanById[node.id]!!.status
            if (!currentStatus.isOneOf(ICE_PROTECTED_3, FULLY_SCANNED_4)) return@forEach

            run.updateScanStatus(node.id, FULLY_SCANNED_4)
            stompService.toRun(run.runId, ServerActions.SERVER_UPDATE_NODE_STATUS, "nodeId" to node.id, "newStatus" to FULLY_SCANNED_4)

            run.nodeScanById.filterKeys { nodeId -> neighboringNodeIds.contains(nodeId) }
                .forEach { (neighboringNodeId, nodeScan) ->
                    if (nodeScan.status == UNDISCOVERED_0 || nodeScan.status == UNCONNECTABLE_1) {
                        run.updateScanStatus(neighboringNodeId, CONNECTABLE_2)
                        stompService.toRun(run.runId, ServerActions.SERVER_UPDATE_NODE_STATUS, "nodeId" to neighboringNodeId, "newStatus" to CONNECTABLE_2)
                    }
                }
            runEntityService.save(run)
        }
    }

    fun gmRefreshSite(siteId: String) {
        val hackerStates = hackerStateEntityService.findAllHackersInSite(siteId)
        stompService.toSite(
            siteId,
            ServerActions.SERVER_TERMINAL_RECEIVE,
            StompService.TerminalReceive(TERMINAL_MAIN, arrayOf("[info]Site reboot"))
        )
        hackerStates
            .filter { it.activity == HackerActivity.INSIDE }
            .forEach { hackerState ->
                hackerDisconnect(hackerState, "Disconnected (server abort)")
            }
    }

    fun deleteRuns(siteId: String): Int {
        timerEntityService.deleteBySiteId(siteId)
        taskEngine.removeAll(TaskIdentifiers(null, siteId, null))


        val siteName = sitePropertiesEntityService.getBySiteId(siteId).name
        val runs = runEntityService.findAllForSiteId(siteId)
        runs.forEach { deleteRun(it, siteName) }

        return runs.size
    }

    private fun deleteRun(run: Run, siteName: String) {
        userRunLinkEntityService.deleteAllForRun(run)
        runEntityService.delete(run)

        hackerStateEntityService.findAllHackersInRun(run.runId)
            .onEach { hackerState: HackerState ->
                scanInfoService.sendScanInfosOfPlayer(hackerState.userId)
                hackerStateEntityService.leaveSite(hackerState)
                taskEngine.removeForUser(hackerState.userId)
            }

        stompService.toRun(
            run.runId,
            ServerActions.SERVER_NOTIFICATION,
            NotyMessage(NotyType.ERROR, "Error", "Lost network connection to site: ${siteName}")
        )
        // FIXME: properly inform browser that they need to move to home screen
//            stompService.toRun(run.runId, ServerActions.SERVER_HACKER_DC, "-")
    }
}
