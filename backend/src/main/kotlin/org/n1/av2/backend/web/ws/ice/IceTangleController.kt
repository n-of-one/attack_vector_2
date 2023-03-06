package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.service.layerhacking.ice.tangle.IceTangleService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class IceTangleController(
        val iceTangleService: IceTangleService,
        val taskRunner: TaskRunner ) {

    data class EnterInput(val iceId: String)

    @MessageMapping("/ice/tangle/enter")
    fun enter(command: EnterInput, principal: Principal) {
        taskRunner.runTask(principal) { iceTangleService.enter(command.iceId) }
    }

    data class TanglePointMoveInput(val iceId: String,
                                    val pointId: String,
                                    val x: Int,
                                    val y: Int)

    @MessageMapping("/ice/tangle/moved")
    fun movedPoint(command: TanglePointMoveInput, principal: Principal) {
        taskRunner.runTask(principal) { iceTangleService.move(command) }
    }

}