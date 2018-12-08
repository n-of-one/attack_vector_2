package org.n1.mainframe.backend.model.site

import org.n1.mainframe.backend.model.site.enums.ConnectionType

data class Connection (
    val id: String,
    val from: String, // node ID
    val to: String,// node ID
    val type: ConnectionType
)