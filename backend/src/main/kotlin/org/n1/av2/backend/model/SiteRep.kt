package org.n1.av2.backend.model

import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.SiteProperties


data class SiteRep(
    var node: Node,
    val nodes: List<Node>,
    val siteProperties: SiteProperties
) {

}