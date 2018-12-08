package org.n1.mainframe.backend.model.ui

import org.n1.mainframe.backend.model.site.Connection
import org.n1.mainframe.backend.model.site.Site
import org.n1.mainframe.backend.model.site.Node

data class SiteState (
        val site: Site,
        val nodes: List<Node>,
        val connections: List<Connection>
)