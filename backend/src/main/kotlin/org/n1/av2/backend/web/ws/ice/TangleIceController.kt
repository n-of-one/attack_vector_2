package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.layerhacking.ice.tangle.TangleService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class TangleIceController(
    val tangleService: TangleService,
    val userTaskRunner: UserTaskRunner ) {

    data class EnterInput(val iceId: String)

    @MessageMapping("/ice/tangle/enter")
    fun enter(command: EnterInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { tangleService.enter(command.iceId) }
    }

    data class TanglePointMoveInput(val iceId: String,
                                    val pointId: String,
                                    val x: Int,
                                    val y: Int)

    @MessageMapping("/ice/tangle/moved")
    fun movedPoint(command: TanglePointMoveInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { tangleService.move(command) }
    }

}