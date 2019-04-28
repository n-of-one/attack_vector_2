package org.n1.mainframe.backend.service

import org.n1.mainframe.backend.model.ui.ValidationException
import org.n1.mainframe.backend.model.ui.site.command.AddConnection
import org.n1.mainframe.backend.model.ui.site.command.AddNode
import org.n1.mainframe.backend.model.ui.site.command.EditSiteData
import org.n1.mainframe.backend.model.ui.site.command.MoveNode
import org.n1.mainframe.backend.service.site.*
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class EditorService(
        val siteService: SiteService,
        val layoutService: LayoutService,
        val siteDataService: SiteDataService,
        val nodeService: NodeService,
        val connectionService: ConnectionService,
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


    fun update(command: EditSiteData, principal: Principal) {
        siteDataService.update(command, principal)
    }

    fun moveNode(command: MoveNode) {
        nodeService.moveNode(command)
        stompService.toSite(command.siteId, ReduxActions.SERVER_MOVE_NODE, command)
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


}