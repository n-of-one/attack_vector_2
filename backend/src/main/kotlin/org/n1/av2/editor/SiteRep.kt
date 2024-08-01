package org.n1.av2.editor

import org.n1.av2.layer.Layer
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.SiteProperties


data class SiteRep(
    var node: Node,
    val nodes: List<Node>,
    val siteProperties: SiteProperties
) {

    fun findLayer(layerId: String): Layer? {
        val node = nodes.find { node ->
            node.layers.find { it.id == layerId } != null
        } ?: return null
        return node.layers.find { it.id == layerId }
    }
}
