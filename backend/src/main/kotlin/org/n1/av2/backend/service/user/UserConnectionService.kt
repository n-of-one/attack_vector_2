package org.n1.av2.backend.service.user

import org.n1.av2.backend.entity.run.HackerGeneralActivity
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.RunService
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


@Service
class UserConnectionService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val currentUserService: CurrentUserService,
    private val runService: RunService,
    private val stompService: StompService) {

    private val logger = mu.KotlinLogging.logger {}

    /** Returns validity of connection. False means this is a duplicate connection */
    fun connect(userPrincipal: UserPrincipal): Boolean {
        val existingState = hackerStateEntityService.retrieve(userPrincipal.userId)
        return if (existingState.generalActivity == HackerGeneralActivity.OFFLINE) {
            hackerStateEntityService.login(userPrincipal.userId)
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

        val hackerState = hackerStateEntityService.retrieveForCurrentUser()

        if (hackerState.generalActivity == HackerGeneralActivity.RUNNING && hackerState.runId != null) {
            runService.leaveRun(hackerState)
        }

        hackerStateEntityService.goOffline(hackerState)
    }

}