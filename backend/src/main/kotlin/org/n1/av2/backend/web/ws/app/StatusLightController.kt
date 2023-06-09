package org.n1.av2.backend.web.ws.app

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.layerhacking.app.status_light.StatusLightService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class StatusLightController (
    val statusLightService: StatusLightService,
    val taskRunner: TaskRunner
) {

    class StatusLightMessage(val appId: String)
    @MessageMapping("/app/statusLight/enter")
    fun enter(command: StatusLightMessage, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { statusLightService.enter(command.appId) }
    }

    class SetValueMessage(val appId: String, val value: Boolean)
    @MessageMapping("/app/statusLight/setValue")
    fun toggle(command: SetValueMessage, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { statusLightService.setValue(command.appId, command.value) }
    }

}
