package org.n1.av2.backend.service.run

import org.n1.av2.backend.model.db.run.NodeStatus
import org.n1.av2.backend.repo.NodeStatusRepo
import org.n1.av2.backend.util.createId

@org.springframework.stereotype.Service
class NodeStatusService(
        private val nodeStatusRepo: NodeStatusRepo) {

    fun createHackedStatus(nodeId: String, runId: String) {
        val id = createId("nodeStatus-")
        val status = NodeStatus(id, nodeId, runId, true)
        nodeStatusRepo.save(status)
    }


}