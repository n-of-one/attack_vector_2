package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.service.layerhacking.ice.netwalk.NetwalkIceService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class NetwalkIceController(
    val netwalkService: NetwalkIceService,
    val taskRunner: TaskRunner ) {

    data class EnterInput(val iceId: String)
    @MessageMapping("/ice/netwalk/enter")
    fun enter(command: EnterInput, principal: Principal) {
        taskRunner.runTask(principal) { netwalkService.enter(command.iceId) }
    }

    data class RotateInput(val iceId: String, val x: Int, val y: Int)
    @MessageMapping("/ice/netwalk/rotate")
    fun rotate(command: RotateInput, principal: Principal) {
        taskRunner.runTask(principal) { netwalkService.rotate(command.iceId, command.x, command.y) }
    }

}