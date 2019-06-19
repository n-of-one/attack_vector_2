package org.n1.av2.backend.model.ui

data class NotyMessage(
        val type: NotyType,
        val title: String,
        val message: String
)