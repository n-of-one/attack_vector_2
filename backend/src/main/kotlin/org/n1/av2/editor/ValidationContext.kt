package org.n1.av2.editor

import org.n1.av2.layer.Layer
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeRepo
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.SitePropertiesRepo


class ValidationContext(
    var node: Node,
    val nodes: List<Node>,
    val siteProperties: SiteProperties,
    val sitePropertiesRepo: SitePropertiesRepo,
    val nodeRepo: NodeRepo,
) {

    fun findLayer(layerId: String): Layer? {
        val node = nodes.find { node ->
            node.layers.find { it.id == layerId } != null
        } ?: return null
        return node.layers.find { it.id == layerId }
    }
}
