package org.n1.av2.backend.service.run

import org.n1.av2.backend.model.db.run.NodeStatus
import org.n1.av2.backend.repo.NodeStatusRepo
import org.n1.av2.backend.util.createId

@org.springframework.stereotype.Service
class NodeStatusService(
        private val nodeStatusRepo: NodeStatusRepo) {

// FIXME: remove if it compiles without this method
//    fun getNodeStatus(nodeId: String, runId: String): NodeStatus {
//        val status = nodeStatusRepo.findByNodeIdAndRunId(nodeId, runId) ?: NodeStatus("", nodeId, runId, false)
//        return status
//    }
//

    fun createHackedStatus(nodeId: String, runId: String) {
        val id = createId("nodeStatus-")
        val status = NodeStatus(id, nodeId, runId, true)
        nodeStatusRepo.save(status)
    }


}