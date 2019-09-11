package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.SerializingExecutor
import org.n1.av2.backend.service.service.ice.password.IceTangleService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class IceTangleController(
        val iceTangleService: IceTangleService,
        val executor: SerializingExecutor ) {

    data class TanglePointMoveInput(val layerId: String,
                                    val runId: String,
                                    val id: String,
                                    val x: Int,
                                    val y: Int)

    @MessageMapping("/ice/tangle/moved")
    fun scansOfPlayer(command: TanglePointMoveInput, principal: Principal) {
        executor.run(principal) { iceTangleService.move(command) }
    }


}