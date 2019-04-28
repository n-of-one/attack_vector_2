package org.n1.mainframe.backend.model.site

import org.n1.mainframe.backend.model.site.enums.NodeType

data class Node(
        val id: String,
        val siteId: String,
        val type: NodeType,
        var x: Int,
        var y: Int,
        val ice: Boolean,
        val services: List<Service>
)