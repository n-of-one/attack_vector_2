package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.layerhacking.ice.tangle.TangleService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class TangleIceController(
    val tangleService: TangleService,
    val taskRunner: TaskRunner ) {

    data class EnterInput(val iceId: String)

    @MessageMapping("/ice/tangle/enter")
    fun enter(command: EnterInput, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { tangleService.enter(command.iceId) }
    }

    data class TanglePointMoveInput(val iceId: String,
                                    val pointId: String,
                                    val x: Int,
                                    val y: Int)

    @MessageMapping("/ice/tangle/moved")
    fun movedPoint(command: TanglePointMoveInput, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { tangleService.move(command) }
    }

}