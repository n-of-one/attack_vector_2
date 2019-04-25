package org.n1.mainframe.backend.model.ui.site.command


data class AddConnection(
        val siteId:String = "",
        val fromId: String = "",
        val toId: String = ""
)