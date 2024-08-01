package org.n1.av2.layer.ice.tangle

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class TangleIceController(
    val tangleService: TangleService,
    val userTaskRunner: UserTaskRunner
) {

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
