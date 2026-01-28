package org.n1.av2.layer.app.status_light

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class StatusLightController (
    val statusLightService: StatusLightService,
    val userTaskRunner: UserTaskRunner
) {

    class StatusLightMessage(val layerId: String)
    @MessageMapping("/app/statusLight/enter")
    fun enter(command: StatusLightMessage, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/app/statusLight/enter", userPrincipal) { statusLightService.enter(command.layerId) }
    }

    class SetValueMessage(val layerId: String, val value: Boolean)
    @MessageMapping("/app/statusLight/setValue")
    fun toggle(command: SetValueMessage, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/app/statusLight/setValue", userPrincipal) { statusLightService.setValue(command.layerId, command.value) }
    }

}
