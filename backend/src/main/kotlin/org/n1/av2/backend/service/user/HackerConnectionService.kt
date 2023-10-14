package org.n1.av2.backend.service.user

import org.n1.av2.backend.config.websocket.ConnectionType
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.RunService
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


@Service
class HackerConnectionService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val runService: RunService,
    private val stompService: StompService) {

    private val logger = mu.KotlinLogging.logger {}

    fun browserConnect(userPrincipal: UserPrincipal) {
        val hackerState = hackerStateEntityService.retrieve(userPrincipal.userId)
        if (hackerState.activity.inRun) {
            runService.leaveSite(hackerState)
        }
        hackerStateEntityService.login(userPrincipal.userId)

        if (userPrincipal.userEntity.type.hacker) {
            stompService.toUser(userPrincipal.userId, ServerActions.SERVER_USER_CONNECTION,
                "connectionId" to userPrincipal.connectionId, "type" to  ConnectionType.WS_HACKER_MAIN)
        }
    }

    fun sendTime() {
        stompService.reply(ServerActions.SERVER_TIME_SYNC, ZonedDateTime.now())
    }

    fun browserDisconnect(userPrincipal: UserPrincipal) {
        val hackerState = hackerStateEntityService.retrieve(userPrincipal.userId)
        val connectionId = hackerStateEntityService.currentUserConnectionId()

        if (hackerState.connectionId != connectionId) return // don't change the active session over another session disconnecting

        if (hackerState.activity.inRun) {
            runService.leaveSite(hackerState)
        }

        hackerStateEntityService.goOffline(userPrincipal)
    }

}