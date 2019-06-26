package org.n1.av2.backend.model

import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.db.site.SiteData


data class SiteRep(
        var node: Node,
        val nodes: List<Node>,
        val siteData: SiteData
) {

    fun findNodeByServiceId(serviceId: String): Node? {
        val node = nodes.find { node ->
            node.services.filter { service ->
                service.id == serviceId
            }.any()
        }
        return node
    }
}