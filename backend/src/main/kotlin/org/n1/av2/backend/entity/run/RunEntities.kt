package org.n1.av2.backend.entity.run

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime


@Document
data class Run(
    @Id val runId: String,
    val initiatorId: String,
    val siteId: String,
    var totalDistanceScanned: Int = 0,
    var scanStartTime: ZonedDateTime,
    var scanDuration: Int? = null,
    var scanEfficiency: Int? = null,
    val nodeScanById: MutableMap<String, NodeScan>
)

@Document
data class UserRunLink(
    @Id val _mongoId_: String? = null,
    @Indexed val userId: String,
    @Indexed val runId: String
)

@Document
data class TracingPatroller(
    @Id val id: String,
    @Indexed val runId: String,
    val siteId: String,
    val originatingNodeId: String,
    val targetUserId: String,
    var currentNodeId: String,
    val path: MutableList<PatrollerPathSegment>
)

enum class NodeScanStatus(val level: Int) {
    UNDISCOVERED_0(0),             // scan, run: the existence of this node has not been discovered      [ - no image - ]
    DISCOVERED_1(1),               // scan, run: existence is known, but the type of node is not known   [empty.png]
    TYPE_KNOWN_2(2),               // scan, run: type and number of services known                       [type\...]
    CONNECTIONS_KNOWN_3(3),        // scan, run: the connections of this node are known.                 [connections\...]
    FULLY_SCANNED_4(4),            // scan, run: the layers of this node are known                       [free\..., protected\..., hacked\...]
}

data class NodeScan(
    var status: NodeScanStatus,
    var distance: Int? = null
)


class PatrollerPathSegment(
    val fromNodeId: String,
    val toNodeId: String
)
