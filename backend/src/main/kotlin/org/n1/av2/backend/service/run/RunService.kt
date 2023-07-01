package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.TimedTaskRunner
import org.n1.av2.backend.entity.run.*
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.HackerIcon
import org.n1.av2.backend.entity.user.User
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.entity.user.UserType
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.patroller.PatrollerUiData
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.n1.av2.backend.service.scan.ScanInfoService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.terminal.SyntaxHighlightingService
import org.n1.av2.backend.service.user.CurrentUserService
import org.springframework.stereotype.Service

@Service
class RunService(
    private val runEntityService: RunEntityService,
    private val currentUserService: CurrentUserService,
    private val siteService: SiteService,
    private val tracingPatrollerService: TracingPatrollerService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val userEntityService: UserEntityService,
    private val timedTaskRunner: TimedTaskRunner,
    private val syntaxHighlightingService: SyntaxHighlightingService,
    private val stompService: StompService,
    private val userRunLinkEntityService: UserRunLinkEntityService,
    private val nodeEntityService: NodeEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val scanInfoService: ScanInfoService,
) {

    class HackerPresence(
        val userId: String,
        val userName: String,
        val icon: HackerIcon,
        val nodeId: String?,
        val targetNodeId: String?,
        val activity: RunActivity,
        val locked: Boolean
    )

    fun enterRun(runId: String) {
        val run = runEntityService.getByRunId(runId)
        val thisHackerState = hackerStateEntityService.enterScan(run.siteId, runId)

        syntaxHighlightingService.sendForScan()
        stompService.toRun(runId, ServerActions.SERVER_HACKER_ENTER_SCAN, toPresence(thisHackerState))

        val siteFull = siteService.getSiteFull(run.siteId)
        siteFull.sortNodeByDistance(run)

        val hackerPresences = getPresenceInRun(runId)
        val patrollers = tracingPatrollerService.getAllForRun(runId)

        class ScanAndSite(val run: Run, val site: SiteFull, val hackers: List<HackerPresence>, val patrollers: List<PatrollerUiData>)

        val scanAndSite = ScanAndSite(run, siteFull, hackerPresences, patrollers)
        stompService.reply(ServerActions.SERVER_SCAN_FULL, scanAndSite)
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
            targetNodeId = state.currentNodeId,
            activity = state.runActivity,
            locked = state.locked
        )
    }

    fun leaveRun(hackerState: HackerState) {
        val runId = hackerState.runId ?: return // if somehow the user was already disconnected for another reason

        timedTaskRunner.removeAllFor(hackerState.userId)
        tracingPatrollerService.disconnected(hackerState.userId)

        class HackerLeaveNotification(val userId: String)
        stompService.toRun(runId, ServerActions.SERVER_HACKER_LEAVE_SCAN, HackerLeaveNotification(hackerState.userId))
        stompService.reply(ServerActions.SERVER_HACKER_DC, "-")

        hackerStateEntityService.leaveRun(hackerState.userId)
    }

    fun startNewRun(siteName: String) {
        val siteProperties = sitePropertiesEntityService.findByName(siteName)
        if (siteProperties == null) {
            stompService.replyMessage(NotyMessage(NotyType.NEUTRAL, "Error", "Site '${siteName}' not found"))
            return
        }

        val nodeScans = scanInfoService.createNodeScans(siteProperties.siteId)

        val user = currentUserService.user

        val runId = createRun(siteProperties.siteId, nodeScans, user)

        data class ScanSiteResponse(val runId: String, val siteId: String)

        val response = ScanSiteResponse(runId, siteProperties.siteId)
        stompService.reply(ServerActions.SERVER_SITE_DISCOVERED, response)
        scanInfoService.sendScanInfosOfPlayer() // to update the scans in the home screen
    }

    fun createRun(siteId: String, nodeScanById: MutableMap<String, NodeScan>, user: User): String {
        val runId = runEntityService.createRunId()

        val run = runEntityService.create(runId, siteId, nodeScanById, user.id)
        userRunLinkEntityService.createUserScan(run.runId, user)

        return run.runId
    }

    fun deleteSite(siteId: String) {
        val site = sitePropertiesEntityService.getBySiteId(siteId)
        val runs = runEntityService.findAllForSiteId(siteId)

        userRunLinkEntityService.deleteAllForRuns(runs)

        runs.map { run -> hackerStateEntityService.findAllHackersInRun(run.runId) }
            .flatten()
            .onEach { userId: String ->
                scanInfoService.sendScanInfosOfPlayer(userId)
                hackerStateEntityService.leaveRun(userId)
                timedTaskRunner.removeAllFor(userId)
            }

        runs.forEach { run ->
            stompService.toRun(
                run.runId,
                ServerActions.SERVER_NOTIFICATION,
                NotyMessage(NotyType.ERROR, "Error", "Lost network connection to site: ${site.name}")
            )
            stompService.toRun(run.runId, ServerActions.SERVER_HACKER_DC, "-")
        }

        tracingPatrollerService.deleteAllForRuns(runs)
    }
}
