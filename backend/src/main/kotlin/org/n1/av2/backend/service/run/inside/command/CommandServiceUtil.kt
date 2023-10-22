package org.n1.av2.backend.service.run.inside.command

import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.springframework.stereotype.Service

@Service
class CommandServiceUtil(
) {
    fun findBlockingIceLayer(node: Node, runId: String): Layer? {
        val iceLayers = node.layers.filterIsInstance<IceLayer>()
        return iceLayers.findLast { !it.hacked }
    }
}