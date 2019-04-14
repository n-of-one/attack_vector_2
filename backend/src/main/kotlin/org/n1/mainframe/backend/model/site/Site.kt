package org.n1.mainframe.backend.model.site

data class Site(
        val id: String,
        var name: String,
        var description: String = "",
        var creator: String = "",
        var hackTime: String = "15:00",
        var startNodeId: String = "00",
        var hackable: Boolean = false,
        val nodeIds: MutableList<String> = ArrayList(),
        val connectionIds: MutableList<String> = ArrayList()
)
