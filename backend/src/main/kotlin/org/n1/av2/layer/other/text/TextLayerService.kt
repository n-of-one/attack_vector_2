package org.n1.av2.layer.other.text

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.site.entity.Node

@org.springframework.stereotype.Service
class TextLayerService(
    private val connectionService: ConnectionService,
) {

    fun hack(layer: TextLayer, node: Node) {
        connectionService.replyTerminalReceive("Hacked: [pri]${layer.level}[/] ${layer.name}", "")
        connectionService.replyTerminalReceive(layer.text.lines())
    }
}
