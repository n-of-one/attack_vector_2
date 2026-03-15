package org.n1.av2.site.export

import org.n1.av2.editor.SiteFull
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.layer.other.tripwire.TripwireLayer
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
        populateTripwirePortableReferences(siteFull)
        return SiteExportV2(
            exportDetails(),
            siteProperties = toV2(siteFull.siteProperties),
            nodes = siteFull.nodes,
            connections = toV2(siteFull.connections)
        )
    }

    private fun populateTripwirePortableReferences(siteFull: SiteFull) {
        val currentSiteId = siteFull.siteProperties.siteId
        siteFull.nodes.forEach { node ->
            node.layers.filterIsInstance<TripwireLayer>().forEach { tripwire ->
                if (tripwire.coreLayerId == null) return@forEach
                populateTripwireReference(tripwire, currentSiteId, siteFull)
            }
        }
    }

    private fun populateTripwireReference(tripwire: TripwireLayer, currentSiteId: String, siteFull: SiteFull) {
        val isSameSite = tripwire.coreSiteId == null || tripwire.coreSiteId == currentSiteId
        if (isSameSite) {
            populateSameSiteTripwireReference(tripwire, siteFull)
        } else {
            populateCrossSiteTripwireReference(tripwire)
        }
    }

    private fun populateSameSiteTripwireReference(tripwire: TripwireLayer, siteFull: SiteFull) {
        val coreNodeId = deriveNodeIdFromLayerId(tripwire.coreLayerId!!)
        val coreNode = siteFull.nodes.find { it.id == coreNodeId } ?: return
        val coreLayer = coreNode.layers.find { it.id == tripwire.coreLayerId && it is CoreLayer } ?: return
        tripwire.coreSiteName = siteFull.siteProperties.name
        tripwire.coreNetworkId = coreNode.networkId
        tripwire.coreLayerLevel = coreLayer.level
    }

    private fun populateCrossSiteTripwireReference(tripwire: TripwireLayer) {
        try {
            val site = sitePropertiesEntityService.getBySiteId(tripwire.coreSiteId!!)
            val coreNodeId = deriveNodeIdFromLayerId(tripwire.coreLayerId!!)
            val coreNode = nodeEntityService.findById(coreNodeId)
            val coreLayer = coreNode.layers.find { it.id == tripwire.coreLayerId && it is CoreLayer } ?: return
            tripwire.coreSiteName = site.name
            tripwire.coreNetworkId = coreNode.networkId
            tripwire.coreLayerLevel = coreLayer.level
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
