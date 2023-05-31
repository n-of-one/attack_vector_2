package org.n1.av2.backend.config.websocket

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.user.UserConnectionService
import org.n1.av2.backend.service.user.UserIceHackingService
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct


// Prevent circular connection
@Configuration
class StompConnectionEventServiceInit(
    val taskRunner: TaskRunner,
    val userConnectionService: UserConnectionService,
    val stompConnectionEventService: StompConnectionEventService,
    val userIceHackingService: UserIceHackingService
) {

    @PostConstruct
    fun postConstruct() {
        stompConnectionEventService.taskRunner = taskRunner
        stompConnectionEventService.userConnectionService = userConnectionService
        stompConnectionEventService.userIceHackingService = userIceHackingService
    }
}

@Service
class StompConnectionEventService(
    private val currentUserService: CurrentUserService,
) {

    lateinit var taskRunner: TaskRunner
    lateinit var userConnectionService: UserConnectionService
    lateinit var userIceHackingService: UserIceHackingService

    private val logger = mu.KotlinLogging.logger {}

    fun connect(userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) {

            if (userPrincipal.type == ConnectionType.WS_HACKER_MAIN) {
                userConnectionService.connect(userPrincipal)
            }
            if (userPrincipal.type == ConnectionType.WS_NETWORK_APP) {
                userIceHackingService.connect(userPrincipal)
            }
        }
    }

    fun disconnect(userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) {

            if (userPrincipal.type == ConnectionType.WS_HACKER_MAIN) {
                userConnectionService.disconnect(userPrincipal)
            }
            if (userPrincipal.type == ConnectionType.WS_NETWORK_APP) {
                userIceHackingService.disconnect(userPrincipal)
            }
        }
    }

    fun sendTime(userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) {
            userConnectionService.sendTime()
        }
    }

}