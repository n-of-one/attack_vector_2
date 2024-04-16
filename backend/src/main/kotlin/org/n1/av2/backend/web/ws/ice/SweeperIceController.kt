package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
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
        // FIXME networkId
        userTaskRunner.runTask(userPrincipal) { sweeperService.enter(command.iceId, "FIXME") }
    }

    data class SweeperClick(val iceId: String, val x: Int, val y: Int, val leftClick: Boolean)
    @MessageMapping("/ice/sweeper/reveal")
    fun click(command: SweeperClick, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { sweeperService.interact(command.iceId, command.x, command.y, true) }
    }

    @MessageMapping("/ice/sweeper/modify")
    fun modify(command: SweeperClick, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { sweeperService.interact(command.iceId, command.x, command.y, false) }
    }

//    data class RotateInput(val iceId: String, val x: Int, val y: Int)
//    @MessageMapping("/ice/netwalk/rotate")
//    fun rotate(command: RotateInput, userPrincipal: UserPrincipal) {
//        userTaskRunner.runTask(userPrincipal) { netwalkService.rotate(command.iceId, command.x, command.y) }
//    }

}