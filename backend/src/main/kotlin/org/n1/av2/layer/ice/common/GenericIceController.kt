package org.n1.av2.layer.ice.common

import org.n1.av2.layer.app.AppService
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class GenericIceController(
    private val userTaskRunner: UserTaskRunner,
    private val appService: AppService
) {

    data class NextInput(val layerId: String)
    @MessageMapping("/ice/next")
    fun enter(command: org.n1.av2.layer.ice.common.GenericIceController.NextInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { appService.gotoNextLayerAfterExternalHack(command.layerId) }
    }
}
