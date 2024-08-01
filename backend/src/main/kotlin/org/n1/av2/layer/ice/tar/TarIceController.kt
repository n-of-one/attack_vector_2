package org.n1.av2.layer.ice.tar

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class TarIceController(
    val tarService: TarService,
    val userTaskRunner: UserTaskRunner
) {

    data class EnterInput(val iceId: String)
    @MessageMapping("/ice/tar/enter")
    fun enter(command: EnterInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { tarService.enter(command.iceId) }
    }

    data class RotateInput(val iceId: String, val units: Int)
    @MessageMapping("/ice/tar/hackedUnits")
    fun rotate(command: RotateInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { tarService.hackedUnits(command.iceId, command.units) }
    }
}
