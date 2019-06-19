package org.n1.av2.backend.service

import org.n1.av2.backend.model.db.service.Service
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
        return siteDataService.findByName(name)?.id ?: siteService.createSite(name)
    }

    fun addNode(command: AddNode) {
        val layout = layoutService.getById(command.siteId)
        val node = nodeService.createNode(command)
        layoutService.addNode(layout, node)
        stompService.toSite(command.siteId, ReduxActions.SERVER_ADD_NODE, node)
    }

    fun addConnection(command: AddConnection) {
        val layout = layoutService.getById(command.siteId)
        val existing = connectionService.findConnection(command.fromId, command.toId)
        if (existing != null) {
            throw ValidationException("Connection already exists there")
        }

        val connection = connectionService.createConnection(command)

        layoutService.addConnection(layout, connection)
        stompService.toSite(command.siteId, ReduxActions.SERVER_ADD_CONNECTION, connection)
    }

    fun deleteConnections(siteId: String, nodeId: String) {
        val layout = layoutService.getById(siteId)
        val connections = connectionService.findByNodeId(nodeId)
        connectionService.deleteAll(connections)
        layoutService.deleteConnections(layout, connections)

        sendSiteFull(layout.id)
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
        val layout = layoutService.getById(siteId)
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

    data class ServerUpdateService(val nodeId: String, val serviceId: String, val service: Service)
    fun editServiceData(command: EditServiceDataCommand) {
        val node = nodeService.getById(command.nodeId)
        val service = node.services.find{ it.id == command.serviceId} ?: error("Service not found: ${command.serviceId} for ${command.nodeId}")
        service.update(command.key, command.value)
        nodeService.save(node)
        val message = ServerUpdateService(command.nodeId, service.id, service)
        stompService.toSite(command.siteId, ReduxActions.SERVER_UPDATE_SERVICE, message)
        siteValidationService.validate(command.siteId)
    }

    data class ServiceAdded(val nodeId: String, val service: Service)
    fun addService(command: CommandAddService) {
        val service = nodeService.addService(command)
        val message = ServiceAdded(command.nodeId, service)

        stompService.toSite(command.siteId, ReduxActions.SERVER_ADD_SERVICE, message)
        siteValidationService.validate(command.siteId)
    }

    fun removeService(command: CommandRemoveService) {
        val message = nodeService.removeService(command)

        if (message != null) {
            stompService.toSite(command.siteId, ReduxActions.SERVER_NODE_UPDATED, message)
            siteValidationService.validate(command.siteId)
        }
    }

    fun swapServiceLayer(command: CommandSwapService) {
        val message = nodeService.swapServices(command)
        if (message != null) {
            stompService.toSite(command.siteId, ReduxActions.SERVER_NODE_UPDATED, message)
        }
    }
}