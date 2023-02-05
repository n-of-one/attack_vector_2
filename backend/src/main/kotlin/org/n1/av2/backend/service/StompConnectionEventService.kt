package org.n1.av2.backend.service

import mu.KLogging
import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.user.UserConnectionService
import org.springframework.stereotype.Service

@Service
class StompConnectionEventService {

    lateinit var taskRunner: TaskRunner
    lateinit var userConnectionService: UserConnectionService

    companion object : KLogging()

    /** Returns validity of connection. False means this is a duplicate connection */
    fun connect(userPrincipal: UserPrincipal): Boolean {

        // run immediate because we need to immediately check if this is a duplicate connection,
        // and report that back to on this connection request
        return userConnectionService.connect(userPrincipal)
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