package org.n1.mainframe.backend.model.ui

data class DeleteNodeCommand(
        val siteId: String = "",
        val nodeId: String = ""
)