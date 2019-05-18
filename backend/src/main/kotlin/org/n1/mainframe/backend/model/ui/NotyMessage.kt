package org.n1.mainframe.backend.model.ui

data class NotyMessage(
        val type: NotyType,
        val title: String,
        val message: String
)