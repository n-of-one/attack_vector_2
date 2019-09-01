package org.n1.av2.backend.model.ui

import org.n1.av2.backend.model.db.run.LayerStatus
import org.n1.av2.backend.model.db.run.NodeStatus
import org.n1.av2.backend.model.db.run.Scan
import org.n1.av2.backend.model.db.site.*
import org.n1.av2.backend.model.db.site.enums.LayerType
import org.n1.av2.backend.model.db.site.enums.NodeType

data class SiteFull(
        val id: String,
        val siteData: SiteData,
        val layout: Layout,
        val nodes: MutableList<Node>,
        val connections: List<Connection>,
        val state: SiteState,
        val startNodeId: String?,
        var nodeStatuses: List<NodeStatus>?,
        var layerStatuses: List<LayerStatus>?) {

    fun sortNodeByDistance(scan: Scan) {
        nodes.sortBy { node -> scan.nodeScanById[node.id]!!.distance }
    }
}

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


data class EditLayerDataCommand(
        val siteId: String = "",
        val nodeId: String = "",
        val layerId: String = "",
        val key: String = "",
        val value: String = "")

data class AddLayerCommand(
        val siteId: String,
        val nodeId: String,
        val layerType: LayerType)

data class RemoveLayerCommand(
        val siteId: String,
        val nodeId: String,
        val layerId: String)

data class SwapLayerCommand(
        val siteId: String,
        val nodeId: String,
        val fromId: String,
        val toId: String)


