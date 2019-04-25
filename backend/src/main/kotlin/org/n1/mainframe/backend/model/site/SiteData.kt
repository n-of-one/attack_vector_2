package org.n1.mainframe.backend.model.site

data class SiteData (
        val id: String,
        var name: String,
        var description: String = "",
        var creator: String = "",
        var hackTime: String = "15:00",
        var startNodeNetworkId: String = "00",
        var hackable: Boolean = true
)
