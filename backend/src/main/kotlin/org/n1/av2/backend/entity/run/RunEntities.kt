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
data class HackerState(
    @Id val userId: String,
    val connectionId: String,
    val runId: String?,
    val siteId: String?,
    val currentNodeId: String?,
    val previousNodeId: String?,
    val targetNodeId: String?, // target of current move TODO can we remove this?
    val generalActivity: HackerGeneralActivity,
    val runActivity: RunActivity,
    val hookPatrollerId: String?, // Hooked means that a patroller has either locked the hacker, is about to lock the hacker and they cannot escape. TODO remove?
    val locked: Boolean
) {

    fun toRunState(): HackerStateRunning {
        return HackerStateRunning(
            userId, connectionId,
            runId ?: error("runId null for ${userId}"),
            siteId ?: error("siteId null for ${userId}"),
            currentNodeId ?: error("currentNodeId null for ${userId}"),
            previousNodeId, targetNodeId, runActivity, hookPatrollerId, locked
        )
    }
}

@Document
data class LayerStatus(
    @Id val id: String,
    val layerId: String,
    @Indexed val runId: String,
    var hacked: Boolean,
    val hackedBy: MutableList<String>,
    val iceId: String?,
)

@Document
data class NodeStatus(
    @Id val id: String,
    val nodeId: String,
    @Indexed val runId: String,
    var hacked: Boolean
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

enum class HackerGeneralActivity {
    OFFLINE,    // this hacker is not online
    ONLINE,     // this hacker is online, but not in a run
    RUNNING,    // this hacker is in a run
}

enum class RunActivity {
    NA,         // not in a run
    SCANNING,   // hacker has not yet started the attack
    AT_NODE,    // hacker is at rest at a node
}


/** Convenience class that mimicks HackerState but enforces non-null state of all fields that are used in a run */
class HackerStateRunning(
    val userId: String,
    val connectionId: String,
    val runId: String,
    val siteId: String,
    val currentNodeId: String,
    val previousNodeId: String?,
    val targetNodeId: String?,
    val runActivity: RunActivity,
    val hookPatrollerId: String?,
    val locked: Boolean
) {

    fun toState(): HackerState {
        return HackerState(
            userId, connectionId, runId, siteId, currentNodeId, previousNodeId, targetNodeId,
            HackerGeneralActivity.RUNNING, runActivity, hookPatrollerId, locked
        )
    }
}