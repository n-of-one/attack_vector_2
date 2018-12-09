package org.n1.mainframe.backend.service

import org.n1.mainframe.backend.model.site.Site
import org.n1.mainframe.backend.model.ui.*
import org.n1.mainframe.backend.service.site.ConnectionService
import org.n1.mainframe.backend.service.site.NodeService
import org.n1.mainframe.backend.service.site.SiteDataService
import org.n1.mainframe.backend.service.site.SiteService
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class EditorService(
        val siteService: SiteService,
        val siteDataService: SiteDataService,
        val nodeService: NodeService,
        val connectionService: ConnectionService,
        val stompService: StompService) {

    fun getByLinkOrCreate(link: String): Site {
        return siteService.findByLink(link) ?: siteService.createSite(link)
    }

    fun addNode(command: AddNode) {
        val site = siteService.getById(command.siteId)
        val node = nodeService.createNode(command)
        siteService.addNode(site, node)
        stompService.toSite(command.siteId, "SERVER_ADD_NODE", node)
    }

    fun addConnection(command: AddConnection) {
        val site = siteService.getById(command.siteId)
        val existing = connectionService.findConnection(command.from, command.to)
        if (existing != null) {
            throw ValidationException("Connection already exists there")
        }

        val connection = connectionService.createConnection(command)

        siteService.addConnection(site, connection)
        stompService.toSite(command.siteId, "SERVER_ADD_CONNECTION", connection)
    }

    fun purgeAll() {
        siteService.findAll().forEach{ site ->
            stompService.toSite(site.id, "SERVER_FORCE_DISCONNECT", NotyMessage("fatal", "Admin action", "Purging all sites."))
        }

        siteService.purgeAll()
        nodeService.purgeAll()
        connectionService.purgeAll()
    }

    fun deleteConnections(command: DeleteNodeCommand) {
        val site = siteService.getById(command.siteId)
        val connections = connectionService.findByNodeId(command.nodeId)
        connectionService.deleteAll(connections)
        siteService.deleteConnections(site, connections)

        sendSiteFull(site.id)
    }

    fun update(command: EditSiteData, principal: Principal) {
        siteDataService.update(command, principal)
    }

    fun moveNode(command: MoveNode) {
        nodeService.moveNode(command)
        stompService.toSite(command.siteId, "SERVER_MOVE_NODE", command)
    }

    fun sendSiteFull(siteId: String) {
        val site = siteService.getById(siteId)
        val nodes = nodeService.getAll(site.nodes)
        val connections = connectionService.getAll(site.connections)

        val siteState = SiteState(site, nodes, connections)
        stompService.toSite(siteId, "SERVER_SITE_FULL", siteState)
    }

    fun deleteNode(command: DeleteNodeCommand) {
        deleteConnections(command)
        siteService.deleteNode(command)
        nodeService.deleteNode(command.nodeId)

        sendSiteFull(command.siteId)
    }

    fun snap(command: SiteCommand) {
        val site = siteService.getById(command.siteId)
        nodeService.snap(site.nodes)
        sendSiteFull(command.siteId)
    }


}