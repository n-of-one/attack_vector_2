package org.n1.av2.backend.service.layerhacking.app

import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.other.StatusLightLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.util.StompService

@org.springframework.stereotype.Service
class StatusLightLayerService(
    val stompService: StompService,
    val nodeEntityService: NodeEntityService,
) {

    fun hack(layer: StatusLightLayer) {
        data class EnterIce(val layerId: String, val type: String = "switch")
        stompService.reply(ServerActions.SERVER_REDIRECT_CONNECT_APP, EnterIce(layer.id))
        stompService.replyTerminalReceive("Opened in new window.")
    }

}

