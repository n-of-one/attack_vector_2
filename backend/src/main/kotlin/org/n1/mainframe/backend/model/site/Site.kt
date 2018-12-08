package org.n1.mainframe.backend.model.site

data class Site(
        val id: String,
        var link: String,
        var name: String? = null,
        var description: String? = null,
        var creator: String? = null,
        var hackTime: String? = null,
        var startNode: String? = null,
        var hackable: Boolean? = false,
        val nodes: MutableList<String> = ArrayList(),
        val connections: MutableList<String> = ArrayList()
)