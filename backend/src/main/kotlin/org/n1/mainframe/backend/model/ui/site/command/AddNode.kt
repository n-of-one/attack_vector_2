package org.n1.mainframe.backend.model.ui.site.command

import org.n1.mainframe.backend.model.site.enums.NodeType

data class AddNode(
        val siteId:String = "",
        val x: Int = 0,
        val y: Int = 0,
        val type: NodeType = NodeType.DATA_STORE
)