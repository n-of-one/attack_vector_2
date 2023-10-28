package org.n1.av2.backend.model

import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.SiteProperties
import org.n1.av2.backend.entity.site.layer.Layer


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