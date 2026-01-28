package org.n1.av2.layer.ice.sweeper

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class SweeperIceController(
    val sweeperService: SweeperService,
    val userTaskRunner: UserTaskRunner
) {

    data class SweeperEnterInput(val iceId: String)
    @MessageMapping("/ice/sweeper/enter")
    fun enter(command: SweeperEnterInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/ice/sweeper/enter", userPrincipal) { sweeperService.enter(command.iceId) }
    }

    data class SweeperInteract(val iceId: String, val x: Int, val y: Int, val action: SweeperModifyAction)
    @MessageMapping("/ice/sweeper/interact")
    fun click(command: SweeperInteract, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/ice/sweeper/interact", userPrincipal) { sweeperService.interact(command.iceId, command.x, command.y, command.action) }
    }

    data class SweeperResetAction(val iceId: String)
    @MessageMapping("/ice/sweeper/startReset")
    fun startReset(command: SweeperResetAction, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/ice/sweeper/startReset", userPrincipal) { sweeperService.startReset(command.iceId) }
    }

    @MessageMapping("/ice/sweeper/stopReset")
    fun stopReset(command: SweeperResetAction, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/ice/sweeper/stopReset", userPrincipal) { sweeperService.stopReset(command.iceId) }
    }

    @MessageMapping("/ice/sweeper/completeReset")
    fun completeReset(command: SweeperResetAction, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/ice/sweeper/completeReset", userPrincipal) { sweeperService.completeReset(command.iceId) }
    }

}
