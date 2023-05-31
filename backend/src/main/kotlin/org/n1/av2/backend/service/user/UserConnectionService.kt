package org.n1.av2.backend.service.user

import org.n1.av2.backend.config.websocket.ConnectionType
import org.n1.av2.backend.entity.run.HackerGeneralActivity
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ServerActions
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
        hackerStateEntityService.login(userPrincipal.userId)

        class NewConnection(val connectionId: String, val type: ConnectionType)
        if (userPrincipal.user.type.hacker) {
            stompService.toUser(userPrincipal.userId, ServerActions.SERVER_USER_CONNECTION, NewConnection(userPrincipal.connectionId, ConnectionType.WS_HACKER_MAIN))
        }
    }

    fun sendTime() {
        stompService.reply(ServerActions.SERVER_TIME_SYNC, ZonedDateTime.now())
    }

    fun disconnect(userPrincipal: UserPrincipal) {
        if (currentUserService.isSystemUser) return

        val hackerState = hackerStateEntityService.retrieve(userPrincipal.userId)
        val connectionId = hackerStateEntityService.currentUserConnectionId()

        if (hackerState.connectionId != connectionId) return // don't change the active session over another session disconnecting

        if (hackerState.generalActivity == HackerGeneralActivity.RUNNING && hackerState.runId != null) {
            runService.leaveRun(hackerState)
        }

        hackerStateEntityService.goOffline(userPrincipal)
    }

}