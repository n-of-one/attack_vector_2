package org.n1.av2.layer.ice.password

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class PasswordIceController(
    val authAppService: AuthAppService,
    val userTaskRunner: UserTaskRunner
) {

    data class EnterInput(val iceId: String)

    @MessageMapping("/ice/password/enter")
    fun enter(command: EnterInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/ice/password/enter", userPrincipal) { authAppService.enter(command.iceId) }
    }

    class SubmitPassword(val iceId: String, val password: String)

    @MessageMapping("/ice/password/submit")
    fun submit(command: SubmitPassword, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/ice/password/submit", userPrincipal) { authAppService.submitAttempt(command.iceId, command.password) }
    }
}
