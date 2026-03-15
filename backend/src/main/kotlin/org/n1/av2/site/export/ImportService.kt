package org.n1.av2.site.export

import org.n1.av2.frontend.model.NotyMessage
import org.n1.av2.frontend.model.NotyType
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.layer.other.tripwire.TripwireLayer
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
) {

    private val logger = mu.KotlinLogging.logger {}

    private val parser = ExportedSiteParser()


    fun import(json: String, importingUserConnectionId: String) {
        try {
            val siteName = importSite(json)
            sendResponse("Imported site", siteName, NotyType.NEUTRAL, importingUserConnectionId)
            siteService.sendSitesList()
        } catch (e: Exception) {
            logger.error(e.message, e)
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

        resolveCrossSiteTripwireReferences(blueprint.nodes, blueprint.siteProperties.siteId)
        val owner = currentUserService.userEntity
        siteCloneService.cloneSite(blueprint, blueprint.siteProperties.name, owner)
        return blueprint.siteProperties.name
    }

    private fun resolveCrossSiteTripwireReferences(nodes: List<Node>, importedSiteId: String) {
        nodes.forEach { node ->
            node.layers.filterIsInstance<TripwireLayer>().forEach { tripwire ->
                if (tripwire.coreSiteName == null) return@forEach
                val isCrossSite = tripwire.coreSiteId != null && tripwire.coreSiteId != importedSiteId
                if (!isCrossSite) return@forEach
                resolveCrossSiteReference(tripwire)
            }
        }
    }

    private fun resolveCrossSiteReference(tripwire: TripwireLayer) {
        val siteName = tripwire.coreSiteName ?: return
        val networkId = tripwire.coreNetworkId ?: return
        val layerLevel = tripwire.coreLayerLevel ?: return

        val targetSite = sitePropertiesEntityService.findByName(siteName)
        if (targetSite == null) {
            tripwire.coreLayerId = null
            tripwire.coreSiteId = null
            return
        }

        val targetNode = nodeEntityService.findByNetworkId(targetSite.siteId, networkId)
        if (targetNode == null) {
            tripwire.coreLayerId = null
            tripwire.coreSiteId = null
            return
        }

        val targetLayer = targetNode.layers.find { it.level == layerLevel && it is CoreLayer }
        if (targetLayer == null) {
            tripwire.coreLayerId = null
            tripwire.coreSiteId = null
            return
        }

        tripwire.coreSiteId = targetSite.siteId
        tripwire.coreLayerId = targetLayer.id
    }
}
