package org.n1.mainframe.backend.model.ui.site

import org.n1.mainframe.backend.model.site.Connection
import org.n1.mainframe.backend.model.site.Layout
import org.n1.mainframe.backend.model.site.Node
import org.n1.mainframe.backend.model.site.SiteData

data class SiteFull (
        val id: String,
        val siteData: SiteData,
        val layout: Layout,
        val nodes: List<Node>,
        val connections: List<Connection>,
        val startNodeId: String?
)