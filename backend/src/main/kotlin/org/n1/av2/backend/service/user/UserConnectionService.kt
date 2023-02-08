package org.n1.av2.backend.service.user

import org.n1.av2.backend.engine.TimedTaskRunner
import org.n1.av2.backend.model.db.run.HackerGeneralActivity
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.n1.av2.backend.service.run.HackerStateService
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


//val runActivities = listOf(HackerActivityType.SCANNING, HackerActivityType.HACKING)

@Service
class UserConnectionService(
        private val hackerStateService: HackerStateService,
        private val currentUserService: CurrentUserService,
        private val timedTaskRunner: TimedTaskRunner,
        private val tracingPatrollerService: TracingPatrollerService,
        private val stompService: StompService) {

    private val logger = mu.KotlinLogging.logger {}

    /** Returns validity of connection. False means this is a duplicate connection */
    fun connect(userPrincipal: UserPrincipal): Boolean {
        val existingState = hackerStateService.retrieve(userPrincipal.userId)
        return if (existingState.generalActivity == HackerGeneralActivity.OFFLINE) {
            hackerStateService.login(userPrincipal.userId)
            true
        }
        else {
            logger.info("Rejected duplicate session from: " + userPrincipal.user.name )
            false
        }
    }

    fun sendTime() {
        stompService.toUser(currentUserService.userId, ReduxActions.SERVER_TIME_SYNC, ZonedDateTime.now())
    }

    fun disconnect() {
        if (currentUserService.isSystemUser || currentUserService.isAdmin) return;

        val state = hackerStateService.retrieveForCurrentUser()

        if (state.generalActivity == HackerGeneralActivity.RUNNING && state.runId != null) {
            notifyLeaveRun(state.userId, state.runId)
        }

        timedTaskRunner.removeAllFor(state.userId)
        tracingPatrollerService.disconnected(state.userId)

        hackerStateService.goOffline(state)
    }

    private fun notifyLeaveRun(userId: String, runId: String) {
        class HackerLeaveNotification(val userId: String)

        stompService.toRun(runId, ReduxActions.SERVER_HACKER_LEAVE_SCAN, HackerLeaveNotification(userId))
    }


}