package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.service.layerhacking.ice.password.IcePasswordService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class IcePasswordController(
        val icePasswordService: IcePasswordService,
        val taskRunner: TaskRunner ) {

    data class EnterInput(val iceId: String)

    @MessageMapping("/ice/password/enter")
    fun enter(command: EnterInput, principal: Principal) {
        taskRunner.runTask(principal) { icePasswordService.enter(command.iceId) }
    }

    @MessageMapping("/ice/password/submit")
    fun submit(command: IcePasswordService.SubmitPassword, principal: Principal) {
        taskRunner.runTask(principal) { icePasswordService.submitAttempt(command) }
    }


}