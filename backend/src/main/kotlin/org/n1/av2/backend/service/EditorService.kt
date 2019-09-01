package org.n1.av2.backend.service

import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.ui.*
import org.n1.av2.backend.service.site.*

@org.springframework.stereotype.Service
class EditorService(
        val siteService: SiteService,
        val layoutService: LayoutService,
        val siteDataService: SiteDataService,
        val nodeService: NodeService,
        val connectionService: ConnectionService,
        val siteValidationService: SiteValidationService,
        val stompService: StompService) {

    fun getByNameOrCreate(name: String): String {
        return siteDataService.findByName(name)?.siteId ?: siteService.createSite(name)
    }

    fun addNode(command: AddNode) {
        val layout = layoutService.getBySiteId(command.siteId)
        val node = nodeService.createNode(command)
        layoutService.addNode(layout, node)
        stompService.toSite(command.siteId, ReduxActions.SERVER_ADD_NODE, node)
    }

    fun addConnection(command: AddConnection) {
        val layout = layoutService.getBySiteId(command.siteId)
        val existing = connectionService.findConnection(command.fromId, command.toId)
        if (existing != null) {
            throw ValidationException("Connection already exists there")
        }

        val connection = connectionService.createConnection(command)

        layoutService.addConnection(layout, connection)
        stompService.toSite(command.siteId, ReduxActions.SERVER_ADD_CONNECTION, connection)
    }

    fun deleteConnections(siteId: String, nodeId: String) {
        val layout = layoutService.getBySiteId(siteId)
        val connections = connectionService.findByNodeId(nodeId)
        connectionService.deleteAll(connections)
        layoutService.deleteConnections(layout, connections)

        sendSiteFull(layout.siteId)
    }


    fun updateSiteData(command: EditSiteData) {
        siteDataService.update(command)
        siteValidationService.validate(command.siteId)
    }

    fun moveNode(command: MoveNode) {
        val node = nodeService.moveNode(command)
        val response = command.copy(x = node.x, y = node.y)
        stompService.toSite(command.siteId, ReduxActions.SERVER_MOVE_NODE, response)
    }


    fun sendSiteFull(siteId: String) {
        val toSend = siteService.getSiteFull(siteId)
        stompService.toSite(siteId, ReduxActions.SERVER_SITE_FULL, toSend)
    }

    fun deleteNode(siteId: String, nodeId: String) {
        deleteConnections(siteId, nodeId)
        layoutService.deleteNode(siteId, nodeId)
        nodeService.deleteNode(nodeId)

        sendSiteFull(siteId)
    }

    fun snap(siteId: String) {
        val layout = layoutService.getBySiteId(siteId)
        nodeService.snap(layout.nodeIds)
        sendSiteFull(siteId)
    }

    data class ServerUpdateNetworkId(val nodeId: String, val networkId: String)
    fun editNetworkId(command: EditNetworkIdCommand) {
        val node = nodeService.getById(command.nodeId)
        val changedNode = node.copy(networkId = command.value)
        nodeService.save(changedNode)
        val message = ServerUpdateNetworkId(command.nodeId, changedNode.networkId)
        stompService.toSite(command.siteId, ReduxActions.SERVER_UPDATE_NETWORK_ID, message)
        siteValidationService.validate(command.siteId)
    }

    data class ServerUpdateLayer(val nodeId: String, val layerId: String, val layer: Layer)
    fun editLayerData(command: EditLayerDataCommand) {
        val node = nodeService.getById(command.nodeId)
        val layer = node.layers.find{ it.id == command.layerId} ?: error("Layer not found: ${command.layerId} for ${command.nodeId}")
        layer.update(command.key, command.value)
        nodeService.save(node)
        val message = ServerUpdateLayer(command.nodeId, layer.id, layer)
        stompService.toSite(command.siteId, ReduxActions.SERVER_UPDATE_LAYER, message)
        siteValidationService.validate(command.siteId)
    }

    data class LayerAdded(val nodeId: String, val layer: Layer)
    fun addLayer(command: AddLayerCommand) {
        val layer = nodeService.addLayer(command)
        val message = LayerAdded(command.nodeId, layer)

        stompService.toSite(command.siteId, ReduxActions.SERVER_ADD_LAYER, message)
        siteValidationService.validate(command.siteId)
    }

    fun removeLayer(command: RemoveLayerCommand) {
        val message = nodeService.removeLayer(command)

        if (message != null) {
            stompService.toSite(command.siteId, ReduxActions.SERVER_NODE_UPDATED, message)
            siteValidationService.validate(command.siteId)
        }
    }

    fun swapLayers(command: SwapLayerCommand) {
        val message = nodeService.swapLayers(command)
        if (message != null) {
            stompService.toSite(command.siteId, ReduxActions.SERVER_NODE_UPDATED, message)
        }
    }
}