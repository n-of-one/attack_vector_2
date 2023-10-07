package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.layerhacking.app.auth.AuthAppService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class PasswordIceController(
    val authAppService: AuthAppService,
    val taskRunner: TaskRunner ) {

    data class EnterInput(val iceId: String)

    @MessageMapping("/ice/password/enter")
    fun enter(command: EnterInput, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { authAppService.enter(command.iceId) }
    }

    class SubmitPassword(val iceId: String, val password: String)

    @MessageMapping("/ice/password/submit")
    fun submit(command: SubmitPassword, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { authAppService.submitAttempt(command.iceId, command.password) }
    }
}
