package org.n1.av2.backend.config.websocket

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.user.HackerConnectionService
import org.n1.av2.backend.service.user.UserIceHackingService
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct


// Prevent circular connection
@Configuration
class StompConnectionEventServiceInit(
    val userTaskRunner: UserTaskRunner,
    val hackerConnectionService: HackerConnectionService,
    val stompConnectionEventService: StompConnectionEventService,
    val userIceHackingService: UserIceHackingService
) {

    @PostConstruct
    fun postConstruct() {
        stompConnectionEventService.userTaskRunner = userTaskRunner
        stompConnectionEventService.hackerConnectionService = hackerConnectionService
        stompConnectionEventService.userIceHackingService = userIceHackingService
    }
}

@Service
class StompConnectionEventService(
    private val currentUserService: CurrentUserService,
) {

    lateinit var userTaskRunner: UserTaskRunner
    lateinit var hackerConnectionService: HackerConnectionService
    lateinit var userIceHackingService: UserIceHackingService

    private val logger = mu.KotlinLogging.logger {}

    fun connect(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {

            if (userPrincipal.type == ConnectionType.WS_HACKER_MAIN) {
                hackerConnectionService.browserConnect(userPrincipal)
            }
            if (userPrincipal.type == ConnectionType.WS_NETWORK_APP) {
                userIceHackingService.browserConnect(userPrincipal)
            }
        }
    }

    fun browserDisconnect(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {

            if (userPrincipal.type == ConnectionType.WS_HACKER_MAIN) {
                hackerConnectionService.browserDisconnect(userPrincipal)
            }
            if (userPrincipal.type == ConnectionType.WS_NETWORK_APP) {
                userIceHackingService.browserDisconnect(userPrincipal)
            }
        }
    }

    fun sendTime(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            hackerConnectionService.sendTime()
        }
    }

}