package org.n1.av2.site.export

import io.github.oshai.kotlinlogging.KotlinLogging
import org.n1.av2.frontend.model.NotyMessage
import org.n1.av2.frontend.model.NotyType
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.layer.other.core.CoreLinkedLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.site.SiteCloneService
import org.n1.av2.site.SiteService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service


@Service
class ImportService(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val connectionService: ConnectionService,
    private val currentUserService: CurrentUserService,
    private val siteService: SiteService,
    private val siteCloneService: SiteCloneService,
    private val parser: ExportedSiteParser,
) {

    private val logger = KotlinLogging.logger {}

    fun import(json: String, importingUserConnectionId: String) {
        try {
            val siteName = importSite(json)
            sendResponse("Imported site", siteName, NotyType.NEUTRAL, importingUserConnectionId)
            siteService.sendSitesList()
        } catch (e: Exception) {
            logger.error(e) { e.message }
            sendResponse("Error", e.message ?: "Import failed.", NotyType.ERROR, importingUserConnectionId)
        }
    }

    private fun sendResponse(title: String, message: String, type: NotyType, importingUserConnectionId: String) {
        connectionService.toUser(importingUserConnectionId, ServerActions.SERVER_NOTIFICATION, NotyMessage(type, title, message))
    }

    fun importSite(json: String): String {
        val blueprint = parser.parse(json)

        val existingSite = sitePropertiesEntityService.findByName(blueprint.siteProperties.name)
        if (existingSite != null) {
            error("Uploading site that already exists: ${blueprint.siteProperties.name}")
        }

        resolveCrossSiteCoreReferences(blueprint.nodes, blueprint.siteProperties.siteId)
        val owner = currentUserService.userEntity
        siteCloneService.cloneSite(blueprint, blueprint.siteProperties.name, owner)
        return blueprint.siteProperties.name
    }

    private fun resolveCrossSiteCoreReferences(nodes: List<Node>, importedSiteId: String) {
        nodes.forEach { node ->
            node.layers.filterIsInstance<CoreLinkedLayer>().forEach { linkedLayer ->
                if (linkedLayer.coreSiteName == null) return@forEach
                val isCrossSite = linkedLayer.coreSiteId != null && linkedLayer.coreSiteId != importedSiteId
                if (!isCrossSite) return@forEach
                resolveCrossSiteReference(linkedLayer)
            }
        }
    }

    private fun resolveCrossSiteReference(linkedLayer: CoreLinkedLayer) {
        val siteName = linkedLayer.coreSiteName ?: return
        val networkId = linkedLayer.coreNetworkId ?: return
        val layerLevel = linkedLayer.coreLayerLevel ?: return

        val targetSite = sitePropertiesEntityService.findByName(siteName)
        if (targetSite == null) {
            linkedLayer.coreLayerId = null
            linkedLayer.coreSiteId = null
            return
        }

        val targetNode = nodeEntityService.findByNetworkId(targetSite.siteId, networkId)
        if (targetNode == null) {
            linkedLayer.coreLayerId = null
            linkedLayer.coreSiteId = null
            return
        }

        val targetLayer = targetNode.layers.find { it.level == layerLevel && it is CoreLayer }
        if (targetLayer == null) {
            linkedLayer.coreLayerId = null
            linkedLayer.coreSiteId = null
            return
        }

        linkedLayer.coreSiteId = targetSite.siteId
        linkedLayer.coreLayerId = targetLayer.id
    }
}
