package org.n1.av2.backend.service.layerhacking

import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.TextLayer
import org.n1.av2.backend.service.StompService

@org.springframework.stereotype.Service
class TextLayerService(
    val stompService: StompService,
    val nodeEntityService: NodeEntityService,
    val hackedUtil: HackedUtil
) {

    fun hack(layer: TextLayer, node: Node, runId: String) {
        nodeEntityService.save(node)
        hackedUtil.nonIceHacked(layer.id, node, runId)

        stompService.terminalReceiveCurrentUser("Hacked: [pri]${layer.level}[/] ${layer.name}", "", layer.text)
    }

}