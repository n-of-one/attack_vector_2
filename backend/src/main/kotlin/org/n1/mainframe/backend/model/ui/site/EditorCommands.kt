package org.n1.mainframe.backend.model.ui.site

import org.n1.mainframe.backend.model.site.*
import org.n1.mainframe.backend.model.site.enums.NodeType
import org.n1.mainframe.backend.model.site.enums.ServiceType

data class SiteFull(
        val id: String,
        val siteData: SiteData,
        val layout: Layout,
        val nodes: List<Node>,
        val connections: List<Connection>,
        val state: SiteState,
        val startNodeId: String?)

data class AddNode(
        val siteId: String = "",
        val x: Int = 0,
        val y: Int = 0,
        val type: NodeType = NodeType.DATA_STORE
)

data class AddConnection(
        val siteId: String = "",
        val fromId: String = "",
        val toId: String = ""
)

data class MoveNode(
        val siteId: String = "",
        val nodeId: String = "",
        val x: Int = 0,
        val y: Int = 0)

data class EditSiteData(
        val siteId: String = "",
        val field: String = "",
        val value: String = "")

data class EditNetworkIdCommand(
        val siteId: String = "",
        val nodeId: String = "",
        val value: String = "")


data class EditServiceDataCommand(
        val siteId: String = "",
        val nodeId: String = "",
        val serviceId: String = "",
        val key: String = "",
        val value: String = "")

data class CommandAddService(
        val siteId: String,
        val nodeId: String,
        val serviceType: ServiceType)

data class CommandRemoveService(
        val siteId: String,
        val nodeId: String,
        val serviceId: String)

data class CommandSwapService(
        val siteId: String,
        val nodeId: String,
        val fromId: String,
        val toId: String)


