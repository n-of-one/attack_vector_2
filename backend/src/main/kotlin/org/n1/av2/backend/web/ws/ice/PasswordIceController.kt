package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.layerhacking.ice.IceAppService
import org.n1.av2.backend.service.layerhacking.ice.password.UserType
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class PasswordIceController(
    val iceAppService: IceAppService,
    val taskRunner: TaskRunner ) {

    data class EnterInput(val iceId: String, val userType: UserType)

    @MessageMapping("/ice/password/enter")
    fun enter(command: EnterInput, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { iceAppService.enter(command.iceId, command.userType) }
    }

    class SubmitPassword(val iceId: String, val password: String, val userType: UserType)

    @MessageMapping("/ice/password/submit")
    fun submit(command: SubmitPassword, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { iceAppService.submitAttempt(command.iceId, command.password, command.userType) }
    }
}
