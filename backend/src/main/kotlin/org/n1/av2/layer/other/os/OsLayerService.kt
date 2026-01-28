package org.n1.av2.layer.other.os

import org.n1.av2.layer.Layer
import org.n1.av2.platform.connection.ConnectionService

@org.springframework.stereotype.Service
class OsLayerService(
    private val connectionService: ConnectionService
) {

    fun hack(layer: Layer) {
        connectionService.replyTerminalReceive("Hacking ${layer.name} reveals nothing new.")
    }
}
