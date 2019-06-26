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

enum class NodeStatus(val level:Int) {
    UNDISCOVERED(0), // scan, run: the existence of this node has not been discovered      [ - no image - ]
    DISCOVERED(1),   // scan, run: existence is known, but the type of node is not known   [discovered]
    TYPE(2),         // scan, run: type and number of services known                       [type]
    CONNECTIONS(3),  // scan, run: the connections of this node are known.                 [type]
    SERVICES(4),     // scan, run: the services of this node are known                     [free, protected, hacked]

}

data class NodeScan(
        var status: NodeStatus,
        var distance: Int? = null
)