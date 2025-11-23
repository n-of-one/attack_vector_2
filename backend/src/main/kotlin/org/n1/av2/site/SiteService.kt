package org.n1.av2.site

import org.n1.av2.editor.AddNode
import org.n1.av2.editor.SiteEditorStateEntityService
import org.n1.av2.editor.SiteFull
import org.n1.av2.editor.SiteValidationService
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.platform.inputvalidation.ValidationException
import org.n1.av2.site.CoreLayerRemovalType.DELETE_SITE_REMOVAL
import org.n1.av2.site.CoreLayerRemovalType.INTERNAL_REMOVAL
import org.n1.av2.site.entity.*
import org.n1.av2.site.entity.enums.NodeType
import org.n1.av2.site.tutorial.SiteCloneService
import org.springframework.stereotype.Service


enum class CoreLayerRemovalType {
    INTERNAL_REMOVAL,
    DELETE_SITE_REMOVAL
}

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
    private val siteCloneService: SiteCloneService,
    private val siteResetService: SiteResetService,
) {


    data class SiteListItem(
        val id: String,
        val name: String,
        val hackable: Boolean,
        val purpose: String,
        val ok: Boolean,
        val mine: Boolean,
        val ownerName: String,
        val gmSite: Boolean
    )

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
                    ownerName = owner,
                    gmSite = userEntityService.isGmOrSystem(it.ownerUserId)
                )
            }
        connectionService.toUser(currentUserService.userId, actionType = ServerActions.SERVER_SITES_LIST, list)
    }

    @Suppress("unused")
    class CoreInfo(val layerId: String, val level: Int, val name: String, val networkId: String, val siteId: String)

    fun sendAllCores() {

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


    private fun lookupUserName(ownerUserId: String, userNamesById: HashMap<String, String>): String {
        if (userNamesById.containsKey(ownerUserId)) {
            return userNamesById[ownerUserId]!!
        }
        val userName = userEntityService.getUserName(ownerUserId)
        userNamesById[ownerUserId] = userName
        return userName
    }

    fun createSite(name: String): String {
        val id = sitePropertiesEntityService.createId()
        sitePropertiesEntityService.create(id, name)
        siteEditorStateEntityService.create(id)
        nodeEntityService.createNode(AddNode(id, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2, NodeType.TRANSIT_2, null))
        siteValidationService.validate(id)

        return id
    }

    fun getSiteFull(siteId: String): SiteFull {
        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        val nodes = nodeEntityService.findBySiteId(siteId).toMutableList()
        val startNodeId = findStartNode(siteProperties.startNodeNetworkId, nodes)?.id
        val connections = connectionEntityService.getAll(siteId)
        val state = siteEditorStateEntityService.getById(siteId)
        val ownerName = userEntityService.getUserName(siteProperties.ownerUserId)

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

        iceService.resetIceForSite(siteId)
        sendSitesList()
    }

    fun checkIfAllowedToDelete(siteId: String, userPrincipal: UserPrincipal) {
        checkSiteOwnership(siteId, userPrincipal)

        // Cannot remove site that has a core that is linked to tripwires
        val coreLayers = nodeEntityService.findAllCores(siteId)
        coreLayers.forEach { coreLayer ->
            val coreUsage = verifyRemoveCoreLayer(siteId, coreLayer.id, DELETE_SITE_REMOVAL) ?: return@forEach
            throw ValidationException("Cannot delete site. It contains nodes with cores that are configured to reset tripwires in other sites: $coreUsage. Remove the link from the tripwire(s) to the core(s) on this site first.")
        }
    }

    fun checkSiteOwnership(siteId: String, userPrincipal: UserPrincipal) {
        val properties = sitePropertiesEntityService.getBySiteId(siteId)
        if (userPrincipal.userEntity.type == UserType.GM) {
            return
        }
        if (properties.ownerUserId != userPrincipal.userId) {
            throw IllegalStateException("You don't own this site.")
        }
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

    fun verifyRemoveLayer(siteId: String, layerId: String) {
        val layer = nodeEntityService.findLayer(layerId)
        if (layer !is CoreLayer) {
            return
        }

        val coreUsage = verifyRemoveCoreLayer(siteId, layerId, INTERNAL_REMOVAL) ?: return
        throw ValidationException("Cannot delete this core. It is used to reset tripewire(s) in: $coreUsage. Remove the link from the tripwire(s) to this core first.")
    }

    fun verifyRemoveCoreLayer(siteId: String, layerId: String, type: CoreLayerRemovalType): String? {
        val nodesWithTripwiresPointingToThisCore = nodeEntityService.findAllTripwiresWithCore(layerId)

        if (nodesWithTripwiresPointingToThisCore.isEmpty()) return null
        if (type == DELETE_SITE_REMOVAL && nodesWithTripwiresPointingToThisCore.all { node -> node.siteId == siteId }) return null

        return nodesWithTripwiresPointingToThisCore
            .distinct()
            .joinToString(", ") { node ->
                val siteName = sitePropertiesEntityService.getBySiteId(node.siteId).name
                "site:$siteName-node:${node.networkId}"
            }
    }

    fun copySite(sourceSiteId: String) {
        val sourceSiteProperties = sitePropertiesEntityService.getBySiteId(sourceSiteId)
        val targetSiteName = makeSiteCopyName(sourceSiteProperties.name)
        val targetSiteId = siteCloneService.cloneSite(sourceSiteProperties, targetSiteName, currentUserService.userEntity)
        siteResetService.refreshSite(targetSiteId)

        sendSitesList()
        connectionService.replyNeutral("Created site: $targetSiteName")
    }

    private fun makeSiteCopyName(sourceName: String): String {
        val suffix = sourceName.substringAfterLast("-", "")
        if (suffix.isEmpty() || suffix.toIntOrNull() == null) {
            return "$sourceName-2"
        }
        val baseName = sourceName.substringBeforeLast("-")
        val newNumber = suffix.toInt() + 1
        return "$baseName-$newNumber"
    }


}
