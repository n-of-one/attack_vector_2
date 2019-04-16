package org.n1.mainframe.backend.model.ui.site.command

data class MoveNode (
        val siteId:String = "",
        val nodeId: String = "",
        val x: Int = 0,
        val y: Int = 0
)