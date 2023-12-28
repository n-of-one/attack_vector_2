package org.n1.av2.backend.service.admin

import org.n1.av2.backend.entity.site.Connection
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.SiteProperties
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.service.util.TimeService
import org.springframework.stereotype.Service

const val V1 = "v1"

class SiteExportV1(
    val exportDetails: ExportDetails,
    val siteProperties: V1SiteProperties,
    val nodes: List<Node>,
    val connections: List<V1Connection>,
)

class V1Connection(
    val fromId: String,
    val toId: String
)

class V1SiteProperties(
    val siteId: String,
    val name: String,
    val description: String,
    val creator: String,
    val startNodeNetworkId: String,
)

@Service
class V1Exporter(
    private val timeService: TimeService,
) {

    fun toV1(siteFull: SiteFull): SiteExportV1 {
        return SiteExportV1(
            exportDetails(),
            siteProperties = toV1(siteFull.siteProperties),
            nodes = siteFull.nodes,
            connections = toV1(siteFull.connections)
        )
    }

    private fun exportDetails(): ExportDetails {
        return ExportDetails(V1, timeService.formatDateTime(timeService.now()))
    }

    private fun toV1(properties: SiteProperties): V1SiteProperties {
        return V1SiteProperties(
            siteId = properties.siteId,
            name = properties.name,
            description = properties.description,
            creator = properties.creator,
            startNodeNetworkId = properties.startNodeNetworkId,
        )
    }

    private fun toV1(connections: List<Connection>): List<V1Connection> {
        return connections.map { connection ->
            V1Connection(connection.fromId, connection.toId)
        }
    }
}
