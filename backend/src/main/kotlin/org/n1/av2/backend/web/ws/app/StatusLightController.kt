package org.n1.av2.backend.web.ws.app

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.layerhacking.app.status_light.StatusLightService
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
        userTaskRunner.runTask(userPrincipal) { statusLightService.enter(command.layerId) }
    }

    class SetValueMessage(val layerId: String, val value: Boolean)
    @MessageMapping("/app/statusLight/setValue")
    fun toggle(command: SetValueMessage, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { statusLightService.setValue(command.layerId, command.value) }
    }

}
