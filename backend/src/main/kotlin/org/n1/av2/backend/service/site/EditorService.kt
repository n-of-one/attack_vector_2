package org.n1.av2.backend.service.site

import org.n1.av2.backend.entity.site.ConnectionEntityService
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.other.StatusLightField.*
import org.n1.av2.backend.entity.site.layer.other.StatusLightLayer
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.*
import org.n1.av2.backend.service.layerhacking.app.status_light.StatusLightService
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.util.StompService

@org.springframework.stereotype.Service
class EditorService(
    private val siteService: SiteService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val siteValidationService: SiteValidationService,
    private val runService: RunService,
    private val stompService: StompService,
    private val statusLightService: StatusLightService,
) {

    fun getByNameOrCreate(name: String): String {
        return sitePropertiesEntityService.findByName(name)?.siteId ?: siteService.createSite(name)
    }

    fun addNode(command: AddNode) {
        val node = nodeEntityService.createNode(command)
        stompService.toSite(command.siteId, ServerActions.SERVER_ADD_NODE, node)
    }

    fun addConnection(command: AddConnection) {
        val existing = connectionEntityService.findConnection(command.fromId, command.toId)
        if (existing != null) {
            throw ValidationException("Connection already exists there")
        }

        val connection = connectionEntityService.createConnection(command)

        stompService.toSite(command.siteId, ServerActions.SERVER_ADD_CONNECTION, connection)
    }

    fun deleteConnections(siteId: String, nodeId: String) {
        deleteConnectionsInternal(nodeId)

        sendSiteFull(siteId)
    }

    private fun deleteConnectionsInternal(nodeId: String) {
        val connections = connectionEntityService.findByNodeId(nodeId)
        connectionEntityService.deleteAll(connections)
    }


    fun updateSiteProperties(command: EditSiteProperty) {
        update(command)
        siteValidationService.validate(command.siteId)
    }

    private fun update(command: EditSiteProperty) {
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
            stompService.toSite(properties.siteId, ServerActions.SERVER_UPDATE_SITE_DATA, properties)
        } catch (validationException: ValidationException) {
            stompService.toSite(properties.siteId, ServerActions.SERVER_UPDATE_SITE_DATA, properties)
            throw validationException
        }
    }

    fun moveNode(command: MoveNode) {
        val node = nodeEntityService.moveNode(command)
        val response = command.copy(x = node.x, y = node.y)
        stompService.toSite(command.siteId, ServerActions.SERVER_MOVE_NODE, response)
    }


    fun sendSiteFull(siteId: String) {
        val toSend = siteService.getSiteFull(siteId)
        stompService.toSite(siteId, ServerActions.SERVER_SITE_FULL, toSend)
    }

    fun deleteNode(siteId: String, nodeId: String) {
        deleteConnectionsInternal(nodeId)
        nodeEntityService.deleteNode(nodeId)

        sendSiteFull(siteId)
    }

    fun snap(siteId: String) {
        nodeEntityService.snap(siteId)
        sendSiteFull(siteId)
    }

    fun center(siteId: String) {
        val properties = sitePropertiesEntityService.getBySiteId(siteId)
        nodeEntityService.center(siteId, properties.startNodeNetworkId)
        sendSiteFull(siteId)
    }
    data class ServerUpdateNetworkId(val nodeId: String, val networkId: String)

    fun editNetworkId(command: EditNetworkIdCommand) {
        val node = nodeEntityService.getById(command.nodeId)
        val changedNode = node.copy(networkId = command.value)
        nodeEntityService.save(changedNode)
        val message = ServerUpdateNetworkId(command.nodeId, changedNode.networkId)
        stompService.toSite(command.siteId, ServerActions.SERVER_UPDATE_NETWORK_ID, message)
        siteValidationService.validate(command.siteId)
    }

    data class ServerUpdateLayer(val nodeId: String, val layerId: String, val layer: Layer)

    fun editLayerData(command: EditLayerDataCommand) {
        val node = nodeEntityService.getById(command.nodeId)
        val layer = node.layers.find { it.id == command.layerId } ?: error("Layer not found: ${command.layerId} for ${command.nodeId}")
        layer.update(command.key, command.value)
        processUpdateToApp(layer, command.key)
        nodeEntityService.save(node)
        sendLayerUpdateMessage(command.siteId, command.nodeId, layer)
        siteValidationService.validate(command.siteId)
    }

    fun sendLayerUpdateMessage(siteId: String, nodeId: String, layer: Layer) {
        val message = ServerUpdateLayer(nodeId, layer.id, layer)
        stompService.toSite(siteId, ServerActions.SERVER_UPDATE_LAYER, message)
    }


    private fun processUpdateToApp(layer: Layer, key: String) {
        if (layer is StatusLightLayer) {
            when (key) {
                STATUS.name -> statusLightService.sendUpdate(layer)
                TEXT_FOR_RED.name -> statusLightService.sendUpdate(layer)
                TEXT_FOR_GREEN.name -> statusLightService.sendUpdate(layer)
                else -> Unit
            }
        }
    }


    data class LayerAdded(val nodeId: String, val layer: Layer)

    fun addLayer(command: AddLayerCommand): Layer {
        val layer = nodeEntityService.addLayer(command)
        val message = LayerAdded(command.nodeId, layer)

        stompService.toSite(command.siteId, ServerActions.SERVER_ADD_LAYER, message)
        siteValidationService.validate(command.siteId)
        return layer
    }

    fun removeLayer(command: RemoveLayerCommand) {
        val message = nodeEntityService.removeLayer(command)

        if (message != null) {
            stompService.toSite(command.siteId, ServerActions.SERVER_NODE_UPDATED, message)
            siteValidationService.validate(command.siteId)
        }
    }

    fun swapLayers(command: SwapLayerCommand) {
        val message = nodeEntityService.swapLayers(command)
        if (message != null) {
            stompService.toSite(command.siteId, ServerActions.SERVER_NODE_UPDATED, message)
        }
    }

    fun deleteSite(siteId: String, userPrincipal: UserPrincipal) {
        runService.deleteRuns(siteId)
        siteService.removeSite(siteId, userPrincipal)
    }

}