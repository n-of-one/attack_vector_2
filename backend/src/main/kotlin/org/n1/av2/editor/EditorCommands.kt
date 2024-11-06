package org.n1.av2.editor

import org.n1.av2.run.entity.Run
import org.n1.av2.site.entity.Connection
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.enums.LayerType
import org.n1.av2.site.entity.enums.NodeType

data class SiteFull(
    val id: String,
    val siteProperties: SiteProperties,
    val ownerName: String,
    val nodes: MutableList<Node>,
    val connections: List<Connection>,
    val state: SiteEditorState,
    val startNodeId: String?,
) {

    fun sortNodeByDistance(run: Run) {
        nodes.sortBy { node -> run.nodeScanById[node.id]!!.distance }
    }
}

data class AddNode(
    val siteId: String,
    val x: Int,
    val y: Int,
    val type: NodeType,
    val networkId: String?,
)

data class AddConnection(
    val siteId: String,
    val fromId: String,
    val toId: String,
)

data class MoveNode(
    val siteId: String,
    val nodeId: String,
    val x: Int,
    val y: Int,
)

data class EditSiteProperty(
    val siteId: String,
    val field: String,
    val value: String,
)

data class EditNetworkIdCommand(
    val siteId: String,
    val nodeId: String,
    val value: String,
)


data class EditLayerDataCommand(
    val siteId: String,
    val nodeId: String,
    val layerId: String,
    val key: String,
    val value: String,
)

data class AddLayerCommand(
    val siteId: String,
    val nodeId: String,
    val layerType: LayerType,
)

data class RemoveLayerCommand(
    val siteId: String,
    val nodeId: String,
    val layerId: String
)

data class SwapLayerCommand(
    val siteId: String,
    val nodeId: String,
    val fromId: String,
    val toId: String
)


