package org.n1.mainframe.backend.model.ui.site.command

import org.n1.mainframe.backend.model.site.enums.ConnectionType

data class AddConnection(
        val siteId:String = "",
        val from: String = "",
        val to: String = "",
        val connectionType: ConnectionType = ConnectionType.DEFAULT
)