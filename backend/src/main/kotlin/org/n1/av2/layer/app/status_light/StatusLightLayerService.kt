package org.n1.av2.layer.app.status_light

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.site.entity.NodeEntityService

@org.springframework.stereotype.Service
class StatusLightLayerService(
    val connectionService: ConnectionService,
    val nodeEntityService: NodeEntityService,
) {

    fun hack(layer: StatusLightLayer) {
        data class EnterIce(val layerId: String, val type: String = "switch")
        connectionService.reply(ServerActions.SERVER_REDIRECT_CONNECT_APP, EnterIce(layer.id))
        connectionService.replyTerminalReceive("Opened in new window.")
    }

}

