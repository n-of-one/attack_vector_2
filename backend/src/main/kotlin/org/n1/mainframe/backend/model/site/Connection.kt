package org.n1.mainframe.backend.model.site


data class Connection (
        val id: String,
        val siteId: String,
        val fromId: String,
        val toId: String
)