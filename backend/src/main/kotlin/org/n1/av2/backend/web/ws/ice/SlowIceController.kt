package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.service.layerhacking.ice.slow.SlowIceService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class SlowIceController(
    val slowIceService: SlowIceService,
    val taskRunner: TaskRunner ) {

    data class EnterInput(val iceId: String)
    @MessageMapping("/ice/slowIce/enter")
    fun enter(command: EnterInput, principal: Principal) {
        taskRunner.runTask(principal) { slowIceService.enter(command.iceId) }
    }

    data class RotateInput(val iceId: String, val units: Int)
    @MessageMapping("/ice/slowIce/hackedUnits")
    fun rotate(command: RotateInput, principal: Principal) {
        taskRunner.runTask(principal) { slowIceService.hackedUnits(command.iceId, command.units) }
    }
}
