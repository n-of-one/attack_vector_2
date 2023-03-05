package org.n1.av2.backend.service.user

import org.n1.av2.backend.entity.run.HackerGeneralActivity
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.model.iam.ConnectionType
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

    fun connect(userPrincipal: UserPrincipal) {
        val existingState = hackerStateEntityService.retrieve(userPrincipal.userId)
         if (existingState.generalActivity == HackerGeneralActivity.OFFLINE) {
            hackerStateEntityService.login(userPrincipal.userId)
         }

        data class NewConnection(val connectionId: String, val type: ConnectionType)
        stompService.toUserAllConnections(userPrincipal.userId, ReduxActions.SERVER_USER_CONNECTION, NewConnection(userPrincipal.connectionId, userPrincipal.type))
    }

    fun sendTime() {
        stompService.reply(ReduxActions.SERVER_TIME_SYNC, ZonedDateTime.now())
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