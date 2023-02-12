package org.n1.av2.backend.service

import org.n1.av2.backend.entity.site.ConnectionEntityService
import org.n1.av2.backend.entity.site.LayoutEntityService
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.model.ui.*
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.site.SiteValidationService

@org.springframework.stereotype.Service
class EditorService(
    val siteService: SiteService,
    val layoutEntityService: LayoutEntityService,
    val sitePropertiesEntityService: SitePropertiesEntityService,
    val nodeEntityService: NodeEntityService,
    val connectionEntityService: ConnectionEntityService,
    val siteValidationService: SiteValidationService,
    val stompService: StompService) {

    fun getByNameOrCreate(name: String): String {
        return sitePropertiesEntityService.findByName(name)?.siteId ?: siteService.createSite(name)
    }

    fun addNode(command: AddNode) {
        val layout = layoutEntityService.getBySiteId(command.siteId)
        val node = nodeEntityService.createNode(command)
        layoutEntityService.addNode(layout, node)
        stompService.toSite(command.siteId, ReduxActions.SERVER_ADD_NODE, node)
    }

    fun addConnection(command: AddConnection) {
        val layout = layoutEntityService.getBySiteId(command.siteId)
        val existing = connectionEntityService.findConnection(command.fromId, command.toId)
        if (existing != null) {
            throw ValidationException("Connection already exists there")
        }

        val connection = connectionEntityService.createConnection(command)

        layoutEntityService.addConnection(layout, connection)
        stompService.toSite(command.siteId, ReduxActions.SERVER_ADD_CONNECTION, connection)
    }

    fun deleteConnections(siteId: String, nodeId: String) {
        deleteConnectionsInternal(siteId, nodeId)

        sendSiteFull(siteId)
    }

    private fun deleteConnectionsInternal(siteId: String, nodeId: String) {
        val layout = layoutEntityService.getBySiteId(siteId)
        val connections = connectionEntityService.findByNodeId(nodeId)
        connectionEntityService.deleteAll(connections)
        layoutEntityService.deleteConnections(layout, connections)
    }


    fun updateSiteData(command: EditSiteData) {
        update(command)
        siteValidationService.validate(command.siteId)
    }

    fun update(command: EditSiteData) {
        val properties = sitePropertiesEntityService.getBySiteId(command.siteId)
        val value = command.value.trim()

        try {
            when (command.field) {
                "name" -> sitePropertiesEntityService.updateName(properties, value)
                "description" -> properties.description = value
                "creator" -> properties.creator = value
                "hackTime" -> properties.hackTime = value
                "startNode" -> properties.startNodeNetworkId = value
                "hackable" -> properties.hackable = value.toBoolean()
                else -> throw IllegalArgumentException("Site field ${command.field} unknown.")
            }

            sitePropertiesEntityService.save(properties)
            stompService.toSite(properties.siteId, ReduxActions.SERVER_UPDATE_SITE_DATA, properties)
        }
        catch (validationException: ValidationException) {
            stompService.toSite(properties.siteId, ReduxActions.SERVER_UPDATE_SITE_DATA, properties)
            throw validationException
        }
    }

    fun moveNode(command: MoveNode) {
        val node = nodeEntityService.moveNode(command)
        val response = command.copy(x = node.x, y = node.y)
        stompService.toSite(command.siteId, ReduxActions.SERVER_MOVE_NODE, response)
    }


    fun sendSiteFull(siteId: String) {
        val toSend = siteService.getSiteFull(siteId)
        stompService.toSite(siteId, ReduxActions.SERVER_SITE_FULL, toSend)
    }

    fun deleteNode(siteId: String, nodeId: String) {
        deleteConnectionsInternal(siteId, nodeId)
        layoutEntityService.deleteNode(siteId, nodeId)
        nodeEntityService.deleteNode(nodeId)

        sendSiteFull(siteId)
    }

    fun snap(siteId: String) {
        val layout = layoutEntityService.getBySiteId(siteId)
        nodeEntityService.snap(layout.nodeIds)
        sendSiteFull(siteId)
    }

    data class ServerUpdateNetworkId(val nodeId: String, val networkId: String)
    fun editNetworkId(command: EditNetworkIdCommand) {
        val node = nodeEntityService.getById(command.nodeId)
        val changedNode = node.copy(networkId = command.value)
        nodeEntityService.save(changedNode)
        val message = ServerUpdateNetworkId(command.nodeId, changedNode.networkId)
        stompService.toSite(command.siteId, ReduxActions.SERVER_UPDATE_NETWORK_ID, message)
        siteValidationService.validate(command.siteId)
    }

    data class ServerUpdateLayer(val nodeId: String, val layerId: String, val layer: Layer)
    fun editLayerData(command: EditLayerDataCommand) {
        val node = nodeEntityService.getById(command.nodeId)
        val layer = node.layers.find{ it.id == command.layerId} ?: error("Layer not found: ${command.layerId} for ${command.nodeId}")
        layer.update(command.key, command.value)
        nodeEntityService.save(node)
        val message = ServerUpdateLayer(command.nodeId, layer.id, layer)
        stompService.toSite(command.siteId, ReduxActions.SERVER_UPDATE_LAYER, message)
        siteValidationService.validate(command.siteId)
    }

    data class LayerAdded(val nodeId: String, val layer: Layer)
    fun addLayer(command: AddLayerCommand) {
        val layer = nodeEntityService.addLayer(command)
        val message = LayerAdded(command.nodeId, layer)

        stompService.toSite(command.siteId, ReduxActions.SERVER_ADD_LAYER, message)
        siteValidationService.validate(command.siteId)
    }

    fun removeLayer(command: RemoveLayerCommand) {
        val message = nodeEntityService.removeLayer(command)

        if (message != null) {
            stompService.toSite(command.siteId, ReduxActions.SERVER_NODE_UPDATED, message)
            siteValidationService.validate(command.siteId)
        }
    }

    fun swapLayers(command: SwapLayerCommand) {
        val message = nodeEntityService.swapLayers(command)
        if (message != null) {
            stompService.toSite(command.siteId, ReduxActions.SERVER_NODE_UPDATED, message)
        }
    }
}