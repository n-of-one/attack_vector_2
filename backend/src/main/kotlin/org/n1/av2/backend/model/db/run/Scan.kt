package org.n1.av2.backend.model.db.run

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document
data class Scan(
        @Id val runId: String,
        val initiatorId: String,
        val siteId: String,
        var totalDistanceScanned: Int = 0,
        var startTime: ZonedDateTime? = null,
        var duration: Int? = null,
        var efficiency: Int? = null,
        val nodeScanById: MutableMap<String, NodeScan>
)

@Document
data class UserScan(
        @Id val _mongoId_: String? = null,
        @Indexed val userId: String,
        @Indexed val runId: String)


// -- Supporting classes -- //

/**
When scanning the status transitions are: DISCOVERED -> TYPE -> CONNECTIONS -> SERVICES

When hacking, the situation is different. There are two situations that occur for hackers with nodes that are not in status: SERVICES

 1. The hacker arrives at a node that is in a state < SERVICES. The hacker then auto-probes the node, and upgrades the status to TYPE
    The hacker does have access to all the service data (via the view command), but the node status will not be updated to this level
    as it would bypass the scanning for connections.

 2. The hacker performs a "hack 0" to hack the OS. This will probe the node for connections. And then upgrade the status to SERVICES
    as the hacker already had access to all service details.
 */
enum class NodeScanStatus(val level:Int) {
    UNDISCOVERED(0),             // scan, run: the existence of this node has not been discovered      [ - no image - ]
    DISCOVERED(1),               // scan, run: existence is known, but the type of node is not known   [discovered]
    TYPE(2),                     // scan, run: type and number of services known                       [type]
    LAYERS_NO_CONNECTIONS(3),    // scan, run: the connections of this node are known.                 [free, protected, hacked]
    CONNECTIONS(4),              // scan, run: the connections of this node are known.                 [type]
    LAYERS(5),                   // scan, run: the layers of this node are known                     [free, protected, hacked]


}

data class NodeScan(
        var status: NodeScanStatus,
        var distance: Int? = null
)