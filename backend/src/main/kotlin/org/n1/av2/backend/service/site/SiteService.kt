package org.n1.av2.backend.service.site

import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class SiteService(
        val stompService: StompService,
        val layoutService: LayoutService,
        val siteDataService: SiteDataService,
        val nodeService: NodeService,
        val connectionService: ConnectionService,
        val siteStateService: SiteStateService
) {

    fun createSite(name: String): String {
        val id = siteDataService.createId()
        siteDataService.create(id, name)
        layoutService.create(id)
        siteStateService.create(id)
        return id
    }


    fun getSiteFull(siteId: String): SiteFull {
        val siteData = siteDataService.getBySiteId(siteId)
        val layout = layoutService.getBySiteId(siteId)
        val nodes = nodeService.getAll(siteId).toMutableList()
        val startNodeId = findStartNode(siteData.startNodeNetworkId, nodes)?.id
        val connections = connectionService.getAll(siteId)
        val state = siteStateService.getById(siteId)

        return SiteFull(siteId, siteData, layout, nodes, connections, state, startNodeId)
    }

    fun findStartNode(startNodeNetworkId: String, nodes: List<Node>): Node? {
        return nodes.find { node -> node.networkId == startNodeNetworkId }
    }

    fun purgeAll() {
        siteDataService.findAll().forEach { siteData ->
            stompService.toSite(siteData.siteId, ReduxActions.SERVER_FORCE_DISCONNECT, NotyMessage(NotyType.FATAL, "Admin action", "Purging all sites."))
        }

        siteDataService.purgeAll()
        layoutService.purgeAll()
        nodeService.purgeAll()
        connectionService.purgeAll()
        siteStateService.purgeAll()
    }


    // --- --- --- //




}