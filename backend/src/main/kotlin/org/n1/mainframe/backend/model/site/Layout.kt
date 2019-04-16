package org.n1.mainframe.backend.model.site

data class Layout (
        val id: String,
        val nodeIds: MutableList<String> = ArrayList(),
        val connectionIds: MutableList<String> = ArrayList()
)
