package org.n1.av2.layer.other.text

import org.n1.av2.platform.connection.ConnectionService
import org.springframework.stereotype.Service

@Service
class TextLayerService(
    private val connectionService: ConnectionService,
) {

    fun hack(layer: TextLayer) {
        connectionService.replyTerminalReceive("Hacked: [pri]${layer.level}[/] ${layer.name}", "")
        connectionService.replyTerminalReceive(layer.text.lines())
    }
}
