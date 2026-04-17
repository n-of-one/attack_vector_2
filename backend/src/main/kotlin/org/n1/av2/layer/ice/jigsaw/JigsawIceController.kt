package org.n1.av2.layer.ice.jigsaw

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class JigsawIceController(
    val jigsawService: JigsawService,
    val userTaskRunner: UserTaskRunner,
) {

    data class JigsawEnterInput(val iceId: String)

    @MessageMapping("/ice/jigsaw/enter")
    fun enter(command: JigsawEnterInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/ice/jigsaw/enter", userPrincipal) { jigsawService.enter(command.iceId) }
    }

    data class JigsawMovedInput(val iceId: String, val groupId: String, val x: Double, val y: Double)

    @MessageMapping("/ice/jigsaw/moved")
    fun moved(command: JigsawMovedInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/ice/jigsaw/moved", userPrincipal) {
            jigsawService.moved(command.iceId, command.groupId, command.x, command.y)
        }
    }

    data class JigsawRotateInput(val iceId: String, val groupId: String, val rotation: Int)

    @MessageMapping("/ice/jigsaw/rotate")
    fun rotate(command: JigsawRotateInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/ice/jigsaw/rotate", userPrincipal) {
            jigsawService.rotate(command.iceId, command.groupId, command.rotation)
        }
    }
}
