package org.n1.av2.run.terminal.inside

import org.n1.av2.layer.Layer
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.site.entity.Node
import org.springframework.stereotype.Service

@Service
class CommandServiceUtil(
) {
    fun findBlockingIceLayer(node: Node, runId: String): Layer? {
        val iceLayers = node.layers.filterIsInstance<IceLayer>()
        return iceLayers.findLast { !it.hacked }
    }
}
