package org.n1.av2.backend.service.site

import org.n1.av2.backend.entity.site.*
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.service.layerhacking.ice.IceService
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.util.ServerFatal
import org.springframework.stereotype.Service

@Service
class SiteService(
    private val stompService: StompService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val siteEditorStateEntityService: SiteEditorStateEntityService,
    private val iceService: IceService,
) {


    data class SiteListItem(val id: String, val name: String, val hackable: Boolean, val creator: String)

    fun sendSitesList() {
        val list = sitePropertiesEntityService.findAll().map { SiteListItem(id = it.siteId, name = it.name, hackable = it.hackable, creator = it.creator) }
        stompService.reply(actionType = ServerActions.SERVER_SITES_LIST, list)
    }

    fun createSite(name: String): String {
        val id = sitePropertiesEntityService.createId()
        sitePropertiesEntityService.create(id, name)
        siteEditorStateEntityService.create(id)
        return id
    }

    fun getSiteFull(siteId: String): SiteFull {
        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        val nodes = nodeEntityService.getAll(siteId).toMutableList()
        val startNodeId = findStartNode(siteProperties.startNodeNetworkId, nodes)?.id
        val connections = connectionEntityService.getAll(siteId)
        val state = siteEditorStateEntityService.getById(siteId)

        return SiteFull(siteId, siteProperties, nodes, connections, state, startNodeId)
    }

    fun findStartNode(startNodeNetworkId: String, nodes: List<Node>): Node? {
        return nodes.find { node -> node.networkId == startNodeNetworkId }
    }

    fun removeSite(siteId: String, userPrincipal: UserPrincipal) {
        stompService.toSite(
            siteId,
            ServerActions.SERVER_ERROR,
            ServerFatal(false, "Site removed by ${userPrincipal.userEntity.name}, please close browser window.")
        )
        sitePropertiesEntityService.delete(siteId)
        nodeEntityService.deleteAllForSite(siteId)
        connectionEntityService.deleteAllForSite(siteId)
        siteEditorStateEntityService.delete(siteId)

        iceService.deleteIce(siteId)
    }


    fun findNeighboringNodeIds(node: Node): List<String> {
        val connections = connectionEntityService.findByNodeId(node.id)
        return connections.map { connection ->
            if (connection.fromId == node.id) connection.toId else connection.fromId
        }
    }

    fun updateHackable(siteId: String, hackable: Boolean) {
        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        val newSiteProperties = siteProperties.copy(hackable = hackable)
        sitePropertiesEntityService.save(newSiteProperties)
        sendSitesList()
    }
}