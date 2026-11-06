package org.n1.av2.site.export

import org.n1.av2.editor.SiteFull
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.layer.other.core.CoreLinkedLayer
import org.n1.av2.platform.util.TimeService
import org.n1.av2.site.entity.*
import org.n1.av2.site.entity.NodeEntityService.Companion.deriveNodeIdFromLayerId
import org.springframework.stereotype.Service

const val V1 = "v1"
const val V2 = "v2"

class SiteExportV2(
    val exportDetails: ExportDetails,
    val siteProperties: V2SiteProperties,
    val nodes: List<Node>,
    val connections: List<V2Connection>,
)

class V2Connection(
    val fromId: String,
    val toId: String
)

class V2SiteProperties(
    val siteId: String,
    val name: String,
    val description: String,
    val purpose: String,
    val startNodeNetworkId: String,
    val nodesLocked: Boolean,
)

@Service
class V2Exporter(
    private val timeService: TimeService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
) {

    fun toV2(siteFull: SiteFull): SiteExportV2 {
        populateCorePortableReferences(siteFull)
        return SiteExportV2(
            exportDetails(),
            siteProperties = toV2(siteFull.siteProperties),
            nodes = siteFull.nodes,
            connections = toV2(siteFull.connections)
        )
    }

    private fun populateCorePortableReferences(siteFull: SiteFull) {
        val currentSiteId = siteFull.siteProperties.siteId
        siteFull.nodes.forEach { node ->
            node.layers.filterIsInstance<CoreLinkedLayer>().forEach { linkedLayer ->
                if (linkedLayer.coreLayerId == null) return@forEach
                populateCoreReference(linkedLayer, currentSiteId, siteFull)
            }
        }
    }

    private fun populateCoreReference(linkedLayer: CoreLinkedLayer, currentSiteId: String, siteFull: SiteFull) {
        val isSameSite = linkedLayer.coreSiteId == null || linkedLayer.coreSiteId == currentSiteId
        if (isSameSite) {
            populateSameSiteCoreReference(linkedLayer, siteFull)
        } else {
            populateCrossSiteCoreReference(linkedLayer)
        }
    }

    private fun populateSameSiteCoreReference(linkedLayer: CoreLinkedLayer, siteFull: SiteFull) {
        val coreNodeId = deriveNodeIdFromLayerId(linkedLayer.coreLayerId!!)
        val coreNode = siteFull.nodes.find { it.id == coreNodeId } ?: return
        val coreLayer = coreNode.layers.find { it.id == linkedLayer.coreLayerId && it is CoreLayer } ?: return
        linkedLayer.coreSiteName = siteFull.siteProperties.name
        linkedLayer.coreNetworkId = coreNode.networkId
        linkedLayer.coreLayerLevel = coreLayer.level
    }

    private fun populateCrossSiteCoreReference(linkedLayer: CoreLinkedLayer) {
        try {
            val site = sitePropertiesEntityService.getBySiteId(linkedLayer.coreSiteId!!)
            val coreNodeId = deriveNodeIdFromLayerId(linkedLayer.coreLayerId!!)
            val coreNode = nodeEntityService.findById(coreNodeId)
            val coreLayer = coreNode.layers.find { it.id == linkedLayer.coreLayerId && it is CoreLayer } ?: return
            linkedLayer.coreSiteName = site.name
            linkedLayer.coreNetworkId = coreNode.networkId
            linkedLayer.coreLayerLevel = coreLayer.level
        } catch (_: Exception) {
            // Cross-site reference is broken (site or node was deleted), skip populating portable fields
        }
    }

    private fun exportDetails(): ExportDetails {
        return ExportDetails(V1, timeService.formatDateTime(timeService.now()))
    }

    private fun toV2(properties: SiteProperties): V2SiteProperties {
        return V2SiteProperties(
            siteId = properties.siteId,
            name = properties.name,
            description = properties.description,
            purpose = properties.purpose,
            startNodeNetworkId = properties.startNodeNetworkId,
            nodesLocked = properties.nodesLocked,
        )
    }

    private fun toV2(connections: List<Connection>): List<V2Connection> {
        return connections.map { connection ->
            V2Connection(connection.fromId, connection.toId)
        }
    }
}
