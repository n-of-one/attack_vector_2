package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.SerializingExecutor
import org.n1.av2.backend.service.service.ice.password.ServiceIcePassword
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class IcePasswordController(
        val serviceIcePassword: ServiceIcePassword,
        val executor: SerializingExecutor ) {


    @MessageMapping("/ice/password/submit")
    fun scansOfPlayer(command: ServiceIcePassword.SubmitPassword, principal: Principal) {
        executor.run(principal) { serviceIcePassword.submitAttempt(command) }
    }


}