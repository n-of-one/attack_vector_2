package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.SerializingExecutor
import org.n1.av2.backend.service.layer.ice.password.IcePasswordService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class IcePasswordController(
        val icePasswordService: IcePasswordService,
        val executor: SerializingExecutor ) {


    @MessageMapping("/ice/password/submit")
    fun scansOfPlayer(command: IcePasswordService.SubmitPassword, principal: Principal) {
        executor.run(principal) { icePasswordService.submitAttempt(command) }
    }


}