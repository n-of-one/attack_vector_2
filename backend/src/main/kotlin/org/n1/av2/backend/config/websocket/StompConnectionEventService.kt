package org.n1.av2.backend.config.websocket

import org.n1.av2.backend.config.websocket.ConnectionType.WS_HACKER_MAIN
import org.n1.av2.backend.config.websocket.ConnectionType.WS_NETWORK_APP
import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.user.HackerConnectionService
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct


// Prevent circular connection
@Configuration
class StompConnectionEventServiceInit(
    val userTaskRunner: UserTaskRunner,
    val hackerConnectionService: HackerConnectionService,
    val stompConnectionEventService: StompConnectionEventService,
) {

    @PostConstruct
    fun postConstruct() {
        stompConnectionEventService.userTaskRunner = userTaskRunner
        stompConnectionEventService.hackerConnectionService = hackerConnectionService
    }
}

@Service
class StompConnectionEventService(
    private val currentUserService: CurrentUserService,
) {

    lateinit var userTaskRunner: UserTaskRunner
    lateinit var hackerConnectionService: HackerConnectionService

    private val logger = mu.KotlinLogging.logger {}

    fun connect(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {

            if (userPrincipal.type == WS_HACKER_MAIN || userPrincipal.type == WS_NETWORK_APP) {
                hackerConnectionService.browserConnect(userPrincipal)
            }
        }
    }

    fun browserDisconnect(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {

            if (userPrincipal.type == WS_HACKER_MAIN || userPrincipal.type == WS_NETWORK_APP) {
                hackerConnectionService.browserDisconnect(userPrincipal)
            }
        }
    }

    fun sendTime(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            hackerConnectionService.sendTime()
        }
    }

}