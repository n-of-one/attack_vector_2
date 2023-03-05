package org.n1.av2.backend.service

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.user.UserConnectionService
import org.springframework.stereotype.Service

@Service
class StompConnectionEventService(
    private val currentUserService: CurrentUserService,
) {

    lateinit var taskRunner: TaskRunner
    lateinit var userConnectionService: UserConnectionService

    private val logger = mu.KotlinLogging.logger {}

    /** Returns validity of connection. False means this is a duplicate connection */
    fun connect(userPrincipal: UserPrincipal) {
        currentUserService.set(userPrincipal.user)
        taskRunner. queueInTicks(1) {
            userConnectionService.connect(userPrincipal)
        }
    }

    fun disconnect(userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) {
            userConnectionService.disconnect()
        }
    }

    fun sendTime(userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) {
            userConnectionService.sendTime()
        }
    }

}