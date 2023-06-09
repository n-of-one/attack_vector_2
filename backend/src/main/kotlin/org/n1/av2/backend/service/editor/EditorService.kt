package org.n1.av2.backend.service.editor

import org.n1.av2.backend.entity.site.ConnectionEntityService
import org.n1.av2.backend.entity.site.LayoutEntityService
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.other.STATUS
import org.n1.av2.backend.entity.site.layer.other.StatusLightLayer
import org.n1.av2.backend.entity.site.layer.other.TEXT_FOR_GREEN
import org.n1.av2.backend.entity.site.layer.other.TEXT_FOR_RED
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.*
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.app.status_light.StatusLightService
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.site.SiteValidationService

@org.springframework.stereotype.Service
class EditorService(
    private val siteService: SiteService,
    private val layoutEntityService: LayoutEntityService,
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
        val layout = layoutEntityService.getBySiteId(command.siteId)
        val node = nodeEntityService.createNode(command)
        layoutEntityService.addNode(layout, node)
        stompService.toSite(command.siteId, ServerActions.SERVER_ADD_NODE, node)
    }

    fun addConnection(command: AddConnection) {
        val layout = layoutEntityService.getBySiteId(command.siteId)
        val existing = connectionEntityService.findConnection(command.fromId, command.toId)
        if (existing != null) {
            throw ValidationException("Connection already exists there")
        }

        val connection = connectionEntityService.createConnection(command)

        layoutEntityService.addConnection(layout, connection)
        stompService.toSite(command.siteId, ServerActions.SERVER_ADD_CONNECTION, connection)
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
        stompService.toSite(command.siteId, ServerActions.SERVER_UPDATE_NETWORK_ID, message)
        siteValidationService.validate(command.siteId)
    }

    data class ServerUpdateLayer(val nodeId: String, val layerId: String, val layer: Layer)

    fun editLayerData(command: EditLayerDataCommand) {
        val node = nodeEntityService.getById(command.nodeId)
        val layer = node.layers.find { it.id == command.layerId } ?: error("Layer not found: ${command.layerId} for ${command.nodeId}")
        layer.update(command.key, command.value)
        processUpdateToApp(layer, command.key, command.value)
        nodeEntityService.save(node)
        val message = ServerUpdateLayer(command.nodeId, layer.id, layer)
        stompService.toSite(command.siteId, ServerActions.SERVER_UPDATE_LAYER, message)
        siteValidationService.validate(command.siteId)
    }

    private fun processUpdateToApp(layer: Layer, key: String, value: String) {
        if (layer is StatusLightLayer) {
            val entity = statusLightService.findById(layer.appId)
            when (key) {
                STATUS -> statusLightService.update(entity.copy(status = value.toBoolean()))
                TEXT_FOR_RED -> statusLightService.update(entity.copy(textForRed = value))
                TEXT_FOR_GREEN -> statusLightService.update(entity.copy(textForGreen = value))
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
        runService.deleteSite(siteId)
        siteService.removeSite(siteId, userPrincipal)
    }

}