package org.n1.av2.site

import org.n1.av2.editor.AddNode
import org.n1.av2.editor.SiteEditorStateEntityService
import org.n1.av2.editor.SiteFull
import org.n1.av2.editor.SiteValidationService
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.platform.util.ServerFatal
import org.n1.av2.site.entity.*
import org.n1.av2.site.entity.enums.NodeType
import org.springframework.stereotype.Service

@Service
class SiteService(
    private val connectionService: ConnectionService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val siteEditorStateEntityService: SiteEditorStateEntityService,
    private val iceService: IceService,
    private val currentUserService: CurrentUserService,
    private val userEntityService: UserEntityService,
    private val siteValidationService: SiteValidationService,
) {


    data class SiteListItem(val id: String, val name: String, val hackable: Boolean, val purpose: String, val ok: Boolean, val mine: Boolean, val ownerName: String)

    fun sendSitesList() {
        val gm = currentUserService.userEntity.type == UserType.GM

        val sites = if (gm) sitePropertiesEntityService.findAll() else sitePropertiesEntityService.findByOwnerUserId(currentUserService.userId)
        val userNamesById = HashMap<String, String>()
        val list = sites
            .map {
                val mine = it.ownerUserId == currentUserService.userId
                val owner = lookupUserName(it.ownerUserId, userNamesById)
                SiteListItem(
                    id = it.siteId,
                    name = it.name,
                    hackable = it.hackable,
                    purpose = it.purpose,
                    ok = it.siteStructureOk,
                    mine = mine,
                    ownerName = owner
                )
            }
        connectionService.toUser(currentUserService.userId, actionType = ServerActions.SERVER_SITES_LIST, list)
    }

    private fun lookupUserName(ownerUserId: String, userNamesById: HashMap<String, String>): String {
        if (userNamesById.containsKey(ownerUserId)) {
            return userNamesById[ownerUserId]!!
        }
        val user = userEntityService.getById(ownerUserId)
        userNamesById[ownerUserId] = user.name
        return user.name
    }


    fun createSite(name: String): String {
        val id = sitePropertiesEntityService.createId()
        sitePropertiesEntityService.create(id, name)
        siteEditorStateEntityService.create(id)
        nodeEntityService.createNode(AddNode(id, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2, NodeType.TRANSIT_2))
        siteValidationService.validate(id)

        return id
    }

    fun getSiteFull(siteId: String): SiteFull {
        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        val nodes = nodeEntityService.getAll(siteId).toMutableList()
        val startNodeId = findStartNode(siteProperties.startNodeNetworkId, nodes)?.id
        val connections = connectionEntityService.getAll(siteId)
        val state = siteEditorStateEntityService.getById(siteId)

        val ownerName = userEntityService.getById(siteProperties.ownerUserId).name

        return SiteFull(siteId, siteProperties, ownerName, nodes, connections, state, startNodeId)
    }

    fun findStartNode(nodes: List<Node>): Node? {
        if (nodes.isEmpty()) {
            return null
        }
        val startNodeNetworkId = sitePropertiesEntityService.getBySiteId(nodes[0].siteId).startNodeNetworkId
        return findStartNode(startNodeNetworkId, nodes)
    }

    fun findStartNode(startNodeNetworkId: String, nodes: List<Node>): Node? {
        return nodes.find { node -> node.networkId == startNodeNetworkId }
    }

    fun removeSite(siteId: String) {
        sitePropertiesEntityService.delete(siteId)
        nodeEntityService.deleteAllForSite(siteId)
        connectionEntityService.deleteAllForSite(siteId)
        siteEditorStateEntityService.delete(siteId)

        iceService.deleteIce(siteId)
        sendSitesList()
    }

    fun checkIfAllowedToDelete(siteId: String, userPrincipal: UserPrincipal) {
        val properties = sitePropertiesEntityService.getBySiteId(siteId)
        if (userPrincipal.userEntity.type == UserType.GM) {
            return
        }
        if (properties.ownerUserId != userPrincipal.userId) {
            throw IllegalStateException("You cannot delete sites of other users.")
        }
        connectionService.toSite(
            siteId,
            ServerActions.SERVER_ERROR,
            ServerFatal(false, "Site removed by ${userPrincipal.userEntity.name}, please close browser window.")
        )
    }

    fun findNeighboringNodeIds(node: Node): List<String> {
        val connections = connectionEntityService.findByNodeId(node.id)
        return connections.map { connection ->
            if (connection.fromId == node.id) connection.toId else connection.fromId
        }
    }

    fun updateHackable(siteId: String, newHackableValue: Boolean) {
        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        if (!siteProperties.siteStructureOk && newHackableValue) {
            connectionService.replyNeutral("The site \"${siteProperties.name}\" has errors. Fix these before making it hackable.")
            return
        }
        val newSiteProperties = siteProperties.copy(hackable = newHackableValue)
        sitePropertiesEntityService.save(newSiteProperties)
        sendSitesList()
    }
}
