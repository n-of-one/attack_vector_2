package org.n1.av2.editor

import org.n1.av2.layer.Layer
import org.n1.av2.layer.app.status_light.StatusLightField.*
import org.n1.av2.layer.app.status_light.StatusLightLayer
import org.n1.av2.layer.app.status_light.StatusLightService
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.platform.inputvalidation.ValidationException
import org.n1.av2.site.SiteService
import org.n1.av2.site.entity.ConnectionEntityService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService

@org.springframework.stereotype.Service
class EditorService(
    private val siteService: SiteService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val siteValidationService: SiteValidationService,
    private val connectionService: ConnectionService,
    private val statusLightService: StatusLightService,
) {

    fun validateAccessToSiteByName(siteName: String, userPrincipal: UserPrincipal) {
        if (userPrincipal.userEntity.type == UserType.GM) {
            return
        } // GM can edit any site
        val siteProperties = sitePropertiesEntityService.findByName(siteName) ?: return // new site
        if (siteProperties.ownerUserId != userPrincipal.userEntity.id) {
            connectionService.replyError("Site already exists")
            error("Site already exists")
        }
    }

    fun validateAccessToSiteById(siteId: String, userPrincipal: UserPrincipal) {
        if (userPrincipal.userEntity.type == UserType.GM) {
            return
        } // GM can edit any site

        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        if (siteProperties.ownerUserId != userPrincipal.userEntity.id) {
            connectionService.replyError("You don't have access to this site")
            error("User ${userPrincipal.userId} tried to edit site: ${siteProperties.name} (${siteId}).")
        }
    }

    fun open(name: String) {
        val siteProperties = sitePropertiesEntityService.findByName(name)
        if (siteProperties != null) {
            connectionService.reply(ServerActions.SERVER_OPEN_EDITOR, "id" to siteProperties.siteId)
            return
        }
        val siteId = siteService.createSite(name)
        connectionService.reply(ServerActions.SERVER_OPEN_EDITOR, "id" to siteId)
    }


    fun addNode(command: AddNode) {
        val node = nodeEntityService.createNode(command)
        siteValidationService.validate(command.siteId)
        connectionService.toSite(command.siteId, ServerActions.SERVER_ADD_NODE, node)
    }

    fun addConnection(command: AddConnection) {
        val existing = connectionEntityService.findConnection(command.fromId, command.toId)
        if (existing != null) {
            throw ValidationException("Connection already exists there")
        }

        val connection = connectionEntityService.createConnection(command)
        siteValidationService.validate(command.siteId)
        connectionService.toSite(command.siteId, ServerActions.SERVER_ADD_CONNECTION, connection)
    }

    fun deleteConnections(siteId: String, nodeId: String) {
        deleteConnectionsInternal(nodeId)
        siteValidationService.validate(siteId)
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
                "plot" -> properties.purpose = value
                "startNode" -> properties.startNodeNetworkId = value
                "hackable" -> properties.hackable = value.toBoolean()
                else -> throw IllegalArgumentException("Site field ${command.field} unknown.")
            }

            sitePropertiesEntityService.save(properties)
            connectionService.toSite(properties.siteId, ServerActions.SERVER_UPDATE_SITE_DATA, properties)
        } catch (validationException: ValidationException) {
            connectionService.toSite(properties.siteId, ServerActions.SERVER_UPDATE_SITE_DATA, properties)
            throw validationException
        }
    }

    fun moveNode(command: MoveNode) {
        val node = nodeEntityService.moveNode(command)
        val response = command.copy(x = node.x, y = node.y)
        connectionService.toSite(command.siteId, ServerActions.SERVER_MOVE_NODE, response)
    }

    fun enter(siteId: String) {
        sendSiteFull(siteId)
        siteService.sendSitesList()
        sendAllCores() // used to configure tripwires with remote cores
    }

    fun sendSiteFull(siteId: String) {
        val toSend = siteService.getSiteFull(siteId)
        connectionService.toSite(siteId, ServerActions.SERVER_SITE_FULL, toSend)
    }

    fun sendAllCores() {
        class CoreInfo(val layerId: String, val level: Int, val name: String, val networkId: String, val siteId: String)

        val allCores: Map<Node, List<CoreLayer>> = nodeEntityService.findAllCores()

        val allCoreInfos: List<CoreInfo> = allCores.map { entry: Map.Entry<Node, List<CoreLayer>> ->
            val node = entry.key
            val coreLayers = entry.value

            coreLayers.map { layer ->
                CoreInfo(
                    layerId = layer.id,
                    level = layer.level,
                    name = layer.name,
                    networkId = node.networkId,
                    siteId = node.siteId,
                )
            }
        }.flatten()

        connectionService.reply(ServerActions.SERVER_ALL_CORE_INFO, allCoreInfos)
    }

    fun deleteNode(siteId: String, nodeId: String) {
        deleteConnectionsInternal(nodeId)
        nodeEntityService.deleteNode(nodeId)
        siteValidationService.validate(siteId)
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
        connectionService.toSite(command.siteId, ServerActions.SERVER_UPDATE_NETWORK_ID, message)
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
        connectionService.toSite(siteId, ServerActions.SERVER_UPDATE_LAYER, message)
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

        connectionService.toSite(command.siteId, ServerActions.SERVER_ADD_LAYER, message)
        siteValidationService.validate(command.siteId)
        return layer
    }

    fun removeLayer(command: RemoveLayerCommand) {
        val message = nodeEntityService.removeLayer(command)

        if (message != null) {
            connectionService.toSite(command.siteId, ServerActions.SERVER_NODE_UPDATED, message)
            siteValidationService.validate(command.siteId)
        }
    }

    fun swapLayers(command: SwapLayerCommand) {
        val message = nodeEntityService.swapLayers(command)
        if (message != null) {
            connectionService.toSite(command.siteId, ServerActions.SERVER_NODE_UPDATED, message)
        }
    }


}
