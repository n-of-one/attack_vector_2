package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.CalledBySystem
import org.n1.av2.backend.engine.TimedTaskRunner
import org.n1.av2.backend.entity.run.*
import org.n1.av2.backend.entity.service.TimerEntityService
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
import org.n1.av2.backend.service.patroller.PatrollerUiData
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.n1.av2.backend.service.run.terminal.SyntaxHighlightingService
import org.n1.av2.backend.service.site.ScanInfoService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
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

        syntaxHighlightingService.sendForScan()
        stompService.toRun(runId, ServerActions.SERVER_HACKER_ENTER_SITE, toPresence(thisHackerState))

        val siteFull = siteService.getSiteFull(run.siteId)
        siteFull.sortNodeByDistance(run)

        val hackerPresences = getPresenceInRun(runId)
        val patrollers = tracingPatrollerService.getAllForRun(runId)

        val timers = tripwireLayerService.findForEnterSite(run.siteId, currentUserService.userId)

        class SiteInfo(val run: Run, val site: SiteFull, val hackers: List<HackerPresence>, val patrollers: List<PatrollerUiData>, val timers: List<TimerInfo>)
        val siteInfo = SiteInfo(run, siteFull, hackerPresences, patrollers, timers)
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
        timedTaskRunner.removeAllFor(hackerState.userId)
        tracingPatrollerService.disconnected(hackerState.userId)

        stompService.toRun(hackerState.runId!!, ServerActions.SERVER_HACKER_DC, "userId" to hackerState.userId)
        stompService.toUser(hackerState.userId, ServerActions.SERVER_TERMINAL_RECEIVE, StompService.TerminalReceive(TERMINAL_MAIN, arrayOf("[info]${message}", "")))
        stompService.toUser(hackerState.userId, ServerActions.SERVER_TERMINAL_UPDATE_PROMPT, "prompt" to "⇀ ", "terminalId" to TERMINAL_MAIN)

        hackerStateEntityService.disconnect(hackerState)
    }


    fun leaveSite(hackerState: HackerState) {
        val runId = hackerState.runId ?: return // if somehow the user was already disconnected for another reason

        timedTaskRunner.removeAllFor(hackerState.userId)
        tracingPatrollerService.disconnected(hackerState.userId)

        class HackerLeaveNotification(val userId: String)
        stompService.toRun(runId, ServerActions.SERVER_HACKER_LEAVE_SITE, HackerLeaveNotification(hackerState.userId))

        hackerStateEntityService.leaveSite(hackerState)
    }

    fun deleteSite(siteId: String) {
        val siteProps = sitePropertiesEntityService.getBySiteId(siteId)
        val runs = runEntityService.findAllForSiteId(siteId)

        userRunLinkEntityService.deleteAllForRuns(runs)

        runs.map { run -> hackerStateEntityService.findAllHackersInRun(run.runId) }
            .flatten()
            .onEach { hackerState: HackerState ->
                scanInfoService.sendScanInfosOfPlayer(hackerState.userId)
                hackerStateEntityService.leaveSite(hackerState)
                timedTaskRunner.removeAllFor(hackerState.userId)
            }

        runs.forEach { run ->
            stompService.toRun(
                run.runId,
                ServerActions.SERVER_NOTIFICATION,
                NotyMessage(NotyType.ERROR, "Error", "Lost network connection to site: ${siteProps.name}")
            )
            // FIXME: properly inform browser that they need to move to home screen
//            stompService.toRun(run.runId, ServerActions.SERVER_HACKER_DC, "-")
        }

        tracingPatrollerService.deleteAllForRuns(runs)
        timerEntityService.deleteBySiteId(siteId)

        timedTaskRunner.removeAllFor(siteId)
    }
}
