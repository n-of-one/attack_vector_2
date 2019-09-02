package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.SerializingExecutor
import org.n1.av2.backend.service.service.ice.password.IcePasswordService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class IceTangleController(
        val icePasswordService: IcePasswordService,
        val executor: SerializingExecutor ) {

    data class TanglePointMoved(val layerId: String,
                                val runId: String,
                                val id: Int,
                                val x: Int,
                                val y: Int)

    @MessageMapping("/ice/tangle/moved")
    fun scansOfPlayer(command: TanglePointMoved, principal: Principal) {
        println(command)
    }


}