package org.n1.av2.backend.service.site

import org.n1.av2.backend.entity.site.*
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class SiteService(
    val stompService: StompService,
    val layoutEntityService: LayoutEntityService,
    val sitePropertiesEntityService: SitePropertiesEntityService,
    val nodeEntityService: NodeEntityService,
    val connectionEntityService: ConnectionEntityService,
    val siteEditorStateEntityService: SiteEditorStateEntityService
) {

    fun createSite(name: String): String {
        val id = sitePropertiesEntityService.createId()
        sitePropertiesEntityService.create(id, name)
        layoutEntityService.create(id)
        siteEditorStateEntityService.create(id)
        return id
    }


    fun getSiteFull(siteId: String): SiteFull {
        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        val layout = layoutEntityService.getBySiteId(siteId)
        val nodes = nodeEntityService.getAll(siteId).toMutableList()
        val startNodeId = findStartNode(siteProperties.startNodeNetworkId, nodes)?.id
        val connections = connectionEntityService.getAll(siteId)
        val state = siteEditorStateEntityService.getById(siteId)

        return SiteFull(siteId, siteProperties, layout, nodes, connections, state, startNodeId, null, null)
    }

    fun findStartNode(startNodeNetworkId: String, nodes: List<Node>): Node? {
        return nodes.find { node -> node.networkId == startNodeNetworkId }
    }


}