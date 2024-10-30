package org.n1.av2.run

import org.n1.av2.editor.SiteFull
import org.n1.av2.frontend.model.NotyMessage
import org.n1.av2.frontend.model.NotyType
import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.layer.other.tripwire.TimerEntityService
import org.n1.av2.layer.other.tripwire.TimerInfo
import org.n1.av2.layer.other.tripwire.TripwireLayerService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.CalledBySystem
import org.n1.av2.platform.engine.TaskEngine
import org.n1.av2.platform.engine.TaskIdentifiers
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.util.isOneOf
import org.n1.av2.run.entity.NodeScanStatus
import org.n1.av2.run.entity.NodeScanStatus.*
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.runlink.RunLinkEntityService
import org.n1.av2.run.runlink.RunLinkService
import org.n1.av2.run.scanning.ScanService
import org.n1.av2.run.terminal.SyntaxHighlightingService
import org.n1.av2.run.terminal.TERMINAL_MAIN
import org.n1.av2.site.SiteService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.SitePropertiesEntityService
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
    private val connectionService: ConnectionService,
    private val runLinkEntityService: RunLinkEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val runLinkService: RunLinkService,
    private val timerEntityService: TimerEntityService,
    private val tripwireLayerService: TripwireLayerService,
    private val scanService: ScanService,
    private val nodeEntityService: NodeEntityService,
    private val hackerEntityService: HackerEntityService,
) {

    class HackerPresence(
        val userId: String,
        val userName: String,
        val icon: HackerIcon,
        val nodeId: String?,
        val activity: HackerActivity,
    )

    class SiteInfo(val run: Run, val site: SiteFull, val hackers: List<HackerPresence>, val timers: List<TimerInfo>)

    fun startNewRun(siteName: String) {
        val siteProperties = sitePropertiesEntityService.findByName(siteName)
        if (siteProperties == null) {
            connectionService.replyMessage(NotyMessage(NotyType.NEUTRAL, "Error", "Site '${siteName}' not found"))
            return
        }
        if (!siteProperties.hackable) {
            replyNotHackable(siteProperties)
            return
        }

        val nodeScanById = scanService.createInitialNodeScans(siteProperties.siteId)
        val run = runEntityService.create(siteProperties.siteId, nodeScanById, currentUserService.userId)
        runLinkEntityService.createRunLink(run.runId, currentUserService.userEntity)

        connectionService.reply(
            ServerActions.SERVER_SITE_DISCOVERED,
            "runId" to run.runId,
            "siteId" to siteProperties.siteId
        )
        runLinkService.sendRunInfosToUser() // to update the scans in the home screen

        // enterRun will be called by the browser, the frontend needs to configure websocket connection for the run.
    }

    fun prepareToEnterRun(runId: String) {
        val run = runEntityService.getByRunId(runId)
        val siteProperties = sitePropertiesEntityService.getBySiteId(run.siteId)
        if (!siteProperties.hackable) {
            replyNotHackable(siteProperties)
            return
        }

        connectionService.reply(ServerActions.SERVER_ENTERING_RUN, "runId" to runId, "siteId" to run.siteId)
    }

    private fun replyNotHackable(siteProperties: SiteProperties) {
        connectionService.replyMessage(
            NotyMessage(
                NotyType.NEUTRAL,
                "Site '${siteProperties.name}'",
                "currently not hackable"
            )
        )
    }

    fun enterRun(userId: String, runId: String, connectionId: String) {
        val run = runEntityService.getByRunId(runId)
        val thisHackerState = hackerStateEntityService.enterRun(run.siteId, userId, runId, connectionId)

        syntaxHighlightingService.sendForOutside()
        connectionService.toRun(runId, ServerActions.SERVER_HACKER_ENTER_SITE, toPresence(thisHackerState))

        val siteFull = siteService.getSiteFull(run.siteId)
        siteFull.sortNodeByDistance(run)

        val hackerPresences = getPresenceInRun(runId)

        val timers = tripwireLayerService.findForEnterSite(run.siteId, userId)

        val siteInfo = SiteInfo(run, siteFull, hackerPresences, timers)
        connectionService.toUser(userId, ServerActions.SERVER_ENTERED_RUN, siteInfo)
    }

    private fun getPresenceInRun(runId: String): List<HackerPresence> {
        val hackersInRun = hackerStateEntityService.getHackersInRun(runId)

        return hackersInRun.map { state -> toPresence(state) }
    }

    private fun toPresence(state: HackerState): HackerPresence {
        val user = userEntityService.getById(state.userId)
        val hacker = hackerEntityService.findForUser(user)

        return HackerPresence(
            userId = user.id,
            userName = user.name,
            icon = hacker.icon,
            nodeId = state.currentNodeId,
            activity = state.activity
        )
    }

    @CalledBySystem
    fun hackerDisconnect(hackerState: HackerState, message: String) {
        taskEngine.removeForUser(hackerState.userId)

        connectionService.toRun(hackerState.runId!!, ServerActions.SERVER_HACKER_DC, "userId" to hackerState.userId)
        connectionService.toUser(
            hackerState.userId,
            ServerActions.SERVER_TERMINAL_RECEIVE,
            ConnectionService.TerminalReceive(TERMINAL_MAIN, arrayOf("[info]${message}", ""))
        )
        connectionService.toUser(
            hackerState.userId,
            ServerActions.SERVER_TERMINAL_UPDATE_PROMPT,
            "prompt" to "⇀ ",
            "terminalId" to TERMINAL_MAIN
        )

        hackerStateEntityService.disconnect(hackerState)
    }


    fun leaveSite(hackerState: HackerState, updateHackerState: Boolean) {
        val runId = hackerState.runId ?: return // if somehow the user was already disconnected for another reason

        taskEngine.removeForUser(hackerState.userId)

        class HackerLeaveNotification(val userId: String)
        connectionService.toRun(runId, ServerActions.SERVER_HACKER_LEAVE_SITE, HackerLeaveNotification(hackerState.userId))

        if (updateHackerState) {
            hackerStateEntityService.leaveSite(hackerState)
        }
    }


    fun enterNetworkedApp(networkedAppId: String) {
        hackerStateEntityService.enterNetworkedApp(networkedAppId)

        val hackerState = hackerStateEntityService.retrieveForCurrentUser().toRunState()
        updateIceHackers(hackerState.runId, networkedAppId)
    }

    class IceHacker(val userId: String, val name: String, val icon: HackerIcon)

    fun updateIceHackers(runId: String, iceId: String) {
        val usersInIce = hackerStateEntityService.findHackersINetworkedApp(runId, iceId)

        val iceHackers = usersInIce.map { userIceHackingState ->
            val user = userEntityService.getById(userIceHackingState.userId)
            val hacker = hackerEntityService.findForUser(user)
            IceHacker(user.id, user.name, hacker.icon)
        }

        connectionService.toIce(
            iceId,
            ServerActions.SERVER_ICE_HACKERS_UPDATED,
            iceHackers
        )

    }


    fun updateNodeStatusToHacked(node: Node) {
        val runs = runEntityService.findAllForSiteId(node.siteId)

        val neighboringNodeIds = siteService.findNeighboringNodeIds(node)

        runs.forEach { run ->
            val nodeScan = run.nodeScanById[node.id]
                ?: // this node did not exist when the previous run was created, probably added later by a GM. Skipping
                return@forEach
            if (!nodeScan.status.isOneOf(ICE_PROTECTED_3, FULLY_SCANNED_4)) return@forEach

            run.updateScanStatus(node.id, FULLY_SCANNED_4)
            connectionService.toRun(
                run.runId,
                ServerActions.SERVER_UPDATE_NODE_STATUS,
                "nodeId" to node.id,
                "newStatus" to FULLY_SCANNED_4
            )

            run.nodeScanById.filterKeys { nodeId -> neighboringNodeIds.contains(nodeId) }
                .forEach { (neighboringNodeId, nodeScan) ->
                    if (nodeScan.status == UNDISCOVERED_0 || nodeScan.status == UNCONNECTABLE_1) {
                        run.updateScanStatus(neighboringNodeId, CONNECTABLE_2)
                        connectionService.toRun(
                            run.runId,
                            ServerActions.SERVER_UPDATE_NODE_STATUS,
                            "nodeId" to neighboringNodeId,
                            "newStatus" to CONNECTABLE_2
                        )
                    }
                }
            runEntityService.save(run)
        }
    }

    fun gmRefreshSite(siteId: String) {
        val hackerStates = hackerStateEntityService.findAllHackersInSite(siteId)
        connectionService.toSite(
            siteId,
            ServerActions.SERVER_TERMINAL_RECEIVE,
            ConnectionService.TerminalReceive(TERMINAL_MAIN, arrayOf("[info]Site reboot"))
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
        runLinkEntityService.deleteAllForRun(run)
        runEntityService.delete(run)

        hackerStateEntityService.findAllHackersInRun(run.runId)
            .onEach { hackerState: HackerState ->
                runLinkService.sendRunInfosToUser(hackerState.userId)
                hackerStateEntityService.leaveSite(hackerState)
                taskEngine.removeForUser(hackerState.userId)
            }

        connectionService.toRun(
            run.runId,
            ServerActions.SERVER_NOTIFICATION,
            NotyMessage(NotyType.ERROR, "Error", "Lost network connection to site: ${siteName}")
        )
        // FIXME: properly inform browser that they need to move to home screen
//            stompService.toRun(run.runId, ServerActions.SERVER_HACKER_DC, "-")
    }

    fun updateRunLinksForResetSite(siteId: String) {

        val nodes = nodeEntityService.getAll(siteId)
        val nodeById = nodes.associateBy { it.id }

        val runs = runEntityService.findAllForSiteId(siteId)
        runs.forEach { run ->
            var changed = false
            val newNodeScansById: Map<String, NodeScanStatus?> = run.nodeScanById.mapValues { (nodeId, nodeScan) ->

                val node = nodeById[nodeId]
                if (node == null) {
                    // node has been removed by GM in the editor
                    changed = true
                    return@mapValues null
                }
                if (node.ice && nodeScan.status == FULLY_SCANNED_4) {
                    changed = true
                    return@mapValues ICE_PROTECTED_3
                }
                nodeScan.status
            }
            if (changed) {
                newNodeScansById.forEach { (nodeId: String, newStatus: NodeScanStatus?) ->
                    if (newStatus == null) {
                        run.nodeScanById.remove(nodeId)
                    }
                    else {
                        run.updateScanStatus(nodeId, newStatus)
                    }
                }
                runEntityService.save(run)
            }
        }
    }

    class TimersInfo(val timers: List<TimerInfo>)

    fun getTimers(runId: String, userId: String) {
        val run = runEntityService.getByRunId(runId)
        val timers = tripwireLayerService.findForEnterSite(run.siteId, userId)

        val timerInfo = TimersInfo(timers)
        connectionService.toUser(userId, ServerActions.SERVER_RUN_TIMER, timerInfo)
    }
}
