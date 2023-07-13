package org.n1.av2.backend.service.site

import org.n1.av2.backend.entity.site.*
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.ice.IceService
import org.n1.av2.backend.util.ServerFatal
import org.springframework.stereotype.Service

@Service
class SiteService(
    private val stompService: StompService,
    private val layoutEntityService: LayoutEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val siteEditorStateEntityService: SiteEditorStateEntityService,
    private val iceService: IceService,
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

        return SiteFull(siteId, siteProperties, layout, nodes, connections, state, startNodeId)
    }

    fun findStartNode(startNodeNetworkId: String, nodes: List<Node>): Node? {
        return nodes.find { node -> node.networkId == startNodeNetworkId }
    }

    fun removeSite(siteId: String, userPrincipal: UserPrincipal) {
        stompService.toSite(siteId, ServerActions.SERVER_ERROR, ServerFatal(false, "Site removed by ${userPrincipal.user.name}, please close browser window."))
        sitePropertiesEntityService.delete(siteId)
        layoutEntityService.delete(siteId)
        nodeEntityService.deleteAllForSite(siteId)
        connectionEntityService.deleteAllForSite(siteId)
        siteEditorStateEntityService.delete(siteId)

        iceService.deleteIce(siteId)
    }

    fun refreshIce(siteId: String) {
        val nodes = nodeEntityService.getAll(siteId)
        nodes.forEach {node ->
            val refreshedNode = node.copy(hacked = false)
            refreshedNode.layers.forEach { layer ->
                if (layer is IceLayer) { layer.hacked = false }
            }

            nodeEntityService.save(refreshedNode)
        }
        iceService.deleteIce(siteId)

        stompService.toSite(siteId, ServerActions.SERVER_REFRESH_ICE, null)
    }




}