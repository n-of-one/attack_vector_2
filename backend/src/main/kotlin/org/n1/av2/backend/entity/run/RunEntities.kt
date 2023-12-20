package org.n1.av2.backend.entity.run

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document


@Document
data class Run(
    @Id val runId: String,
    val initiatorId: String,
    val siteId: String,
    val nodeScanById: MutableMap<String, NodeScan>
) {

    fun updateScanStatus(nodeId: String, status: NodeScanStatus) {
        nodeScanById.compute(nodeId) { _, existingValue ->
            if (existingValue == null) error("run $runId does not contain node $nodeId")
            existingValue.copy(status = status)
        }
    }
}

@Document
data class UserRunLink(
    @Id val _mongoId_: String? = null,
    @Indexed val userId: String,
    @Indexed val runId: String
)

enum class NodeScanStatus(val rank: Int) {
    UNDISCOVERED_0(0),             // the existence of this node has not been discovered                [ - no image - ]
    UNCONNECTABLE_1(1),            // existence is known, but not the network id                        [empty.png]
    CONNECTABLE_2(2),              // existence is known and the network id, but not the type of node   [empty.png]
    ICE_PROTECTED_3(3),            // this node has ICE that is blocking the scan                       [protected\...]
    FULLY_SCANNED_4(4),            // the layers of this node are known                                 [free\..., hacked\...]
}

data class NodeScan(
    val status: NodeScanStatus,
    val distance: Int
)


class PatrollerPathSegment(
    val fromNodeId: String,
    val toNodeId: String
)
