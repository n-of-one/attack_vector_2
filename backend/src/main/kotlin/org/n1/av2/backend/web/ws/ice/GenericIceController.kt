package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.layerhacking.app.AppService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class GenericIceController(
    private val userTaskRunner: UserTaskRunner,
    private val appService: AppService
) {

    data class NextInput(val layerId: String)
    @MessageMapping("/ice/next")
    fun enter(command: NextInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { appService.gotoNextLayerAfterExternalHack(command.layerId) }
    }
}