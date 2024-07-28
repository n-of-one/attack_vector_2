package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.layerhacking.ice.sweeper.SweeperModifyAction
import org.n1.av2.backend.service.layerhacking.ice.sweeper.SweeperService
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
        userTaskRunner.runTask(userPrincipal) { sweeperService.enter(command.iceId) }
    }

    data class SweeperInteract(val iceId: String, val x: Int, val y: Int, val action: SweeperModifyAction)
    @MessageMapping("/ice/sweeper/interact")
    fun click(command: SweeperInteract, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { sweeperService.interact(command.iceId, command.x, command.y, command.action) }
    }

    data class SweeperResetAction(val iceId: String)
    @MessageMapping("/ice/sweeper/startReset")
    fun startReset(command: SweeperResetAction, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { sweeperService.startReset(command.iceId) }
    }

    @MessageMapping("/ice/sweeper/stopReset")
    fun stopReset(command: SweeperResetAction, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { sweeperService.stopReset(command.iceId) }
    }

    @MessageMapping("/ice/sweeper/completeReset")
    fun completeReset(command: SweeperResetAction, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { sweeperService.completeReset(command.iceId) }
    }

}