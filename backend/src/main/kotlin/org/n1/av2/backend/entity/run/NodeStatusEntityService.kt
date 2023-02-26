package org.n1.av2.backend.entity.run

import org.n1.av2.backend.util.createId

@org.springframework.stereotype.Service
class NodeStatusEntityService(
        private val nodeStatusRepo: NodeStatusRepo
) {

    fun createHackedStatus(nodeId: String, runId: String) {
        val id = createId("nodeStatus-")
        val status = NodeStatus(id, nodeId, runId, true)
        nodeStatusRepo.save(status)
    }


}