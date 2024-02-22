package org.n1.av2.backend.service.admin

import org.n1.av2.backend.entity.site.Connection
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.SiteProperties
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.service.util.TimeService
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
)

@Service
class V2Exporter(
    private val timeService: TimeService,
) {

    fun toV2(siteFull: SiteFull): SiteExportV2 {
        return SiteExportV2(
            exportDetails(),
            siteProperties = toV2(siteFull.siteProperties),
            nodes = siteFull.nodes,
            connections = toV2(siteFull.connections)
        )
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
        )
    }

    private fun toV2(connections: List<Connection>): List<V2Connection> {
        return connections.map { connection ->
            V2Connection(connection.fromId, connection.toId)
        }
    }
}
