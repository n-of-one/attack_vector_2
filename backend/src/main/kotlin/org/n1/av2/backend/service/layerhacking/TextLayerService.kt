package org.n1.av2.backend.service.layerhacking

import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.other.TextLayer
import org.n1.av2.backend.service.StompService

@org.springframework.stereotype.Service
class TextLayerService(
    val stompService: StompService,
    val nodeEntityService: NodeEntityService,
) {

    fun hack(layer: TextLayer, node: Node) {
        stompService.replyTerminalReceive("Hacked: [pri]${layer.level}[/] ${layer.name}", "", layer.text)
    }

    fun connect(layer: TextLayer, node: Node) {
        stompService.replyTerminalReceive("Access to UI denied")
    }

}