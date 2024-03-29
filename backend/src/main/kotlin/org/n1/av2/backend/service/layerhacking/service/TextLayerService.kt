package org.n1.av2.backend.service.layerhacking.service

import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.layer.other.TextLayer
import org.n1.av2.backend.service.util.StompService

@org.springframework.stereotype.Service
class TextLayerService(
    private val stompService: StompService,
) {

    fun hack(layer: TextLayer, node: Node) {
        stompService.replyTerminalReceive("Hacked: [pri]${layer.level}[/] ${layer.name}", "", layer.text)
    }
}
