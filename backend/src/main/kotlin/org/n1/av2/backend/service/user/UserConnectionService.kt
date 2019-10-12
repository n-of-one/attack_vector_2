package org.n1.av2.backend.service.user

import mu.KLogging
import org.n1.av2.backend.model.db.run.HackerGeneralActivity
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.HackerStateService
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


//val runActivities = listOf(HackerActivityType.SCANNING, HackerActivityType.HACKING)

@Service
class UserConnectionService(
        private val hackerStateService: HackerStateService,
        private val currentUserService: CurrentUserService,
        private val stompService: StompService) {

    companion object: KLogging()

    /** Returns validity of connection. False means this is a duplicate connection */
    fun connect(userPrincipal: UserPrincipal): Boolean {
        val existingState = hackerStateService.retrieve(userPrincipal.userId)
        return if (existingState.generalActivity == HackerGeneralActivity.OFFLINE) {
            hackerStateService.login(userPrincipal.userId)
            true
        }
        else {
            logger.warn("Rejected duplicate session from: " + userPrincipal.user.name )
            false
        }
    }

    fun sendTime() {
        stompService.toUser(currentUserService.userId, ReduxActions.SERVER_TIME_SYNC, ZonedDateTime.now())
    }

    fun disconnect(userPrincipal: UserPrincipal) {
        val state = hackerStateService.retrieveForCurrentUser()

        if (state.generalActivity == HackerGeneralActivity.RUNNING) {
            notifyLeaveRun(state.userId, state.runId!!)
        }

        hackerStateService.goOffline(state)
    }

    private fun notifyLeaveRun(userId: String, runId: String) {
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_LEAVE_SCAN, HackerLeaveNotification(userId))
    }

    data class HackerLeaveNotification(val userId: String)

}