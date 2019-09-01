package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.layer.TextLayer
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.site.NodeService

@org.springframework.stereotype.Service
class TextLayerService(
        val stompService: StompService,
        val nodeService: NodeService,
        val hackedUtil: HackedUtil
) {

    fun hack(orig: Layer, node: Node, runId: String) {
        val layer = orig as TextLayer

        nodeService.save(node)
        hackedUtil.nonIceHacked(layer.id, node, runId)

        stompService.terminalReceive("Hacked: [pri]${layer.level}[/] ${layer.name}", "", layer.text)
    }

}