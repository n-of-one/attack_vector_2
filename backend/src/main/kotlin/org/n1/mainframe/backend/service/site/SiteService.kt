package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.NETWORK_ID
import org.n1.mainframe.backend.model.site.Node
import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.site.SiteFull
import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class SiteService(
        val stompService: StompService,
        val layoutService: LayoutService,
        val siteDataService: SiteDataService,
        val nodeService: NodeService,
        val connectionService: ConnectionService
        ) {

    fun createSite(name: String): String {
        val id = siteDataService.createId()
        siteDataService.create(id, name)
        layoutService.create(id)
        return id
    }


    fun getSiteFull(siteId: String): SiteFull {
        val siteData = siteDataService.getById(siteId)
        val layout = layoutService.getById(siteId)
        val nodes = nodeService.getAll(layout.nodeIds)
        val startNodeId = findStartNode(siteData.startNodeNetworkId, nodes)?.id
        val connections = connectionService.getAll(layout.connectionIds)

        return SiteFull(siteData, layout, nodes, connections, startNodeId)
    }

    fun findStartNode(startNodeNetworkId: String, nodes: List<Node>): Node? {
        return nodes.find{ node -> node.services[0].data[NETWORK_ID] == startNodeNetworkId }
    }

    fun purgeAll() {
        siteDataService.findAll().forEach{ siteData ->
            stompService.toSite(siteData.id, ReduxActions.SERVER_FORCE_DISCONNECT, NotyMessage("fatal", "Admin action", "Purging all sites."))
        }

        siteDataService.purgeAll()
        layoutService.purgeAll();
        nodeService.purgeAll()
        connectionService.purgeAll()
    }





}