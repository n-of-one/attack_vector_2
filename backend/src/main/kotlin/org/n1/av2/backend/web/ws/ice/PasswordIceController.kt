package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.layerhacking.ice.password.PasswordIceService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class PasswordIceController(
    val passwordIceService: PasswordIceService,
    val taskRunner: TaskRunner ) {

    data class EnterInput(val iceId: String)

    @MessageMapping("/ice/password/enter")
    fun enter(command: EnterInput, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { passwordIceService.enter(command.iceId) }
    }

    @MessageMapping("/ice/password/submit")
    fun submit(command: PasswordIceService.SubmitPassword, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { passwordIceService.submitAttempt(command) }
    }


}