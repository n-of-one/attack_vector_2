package org.n1.av2.script.effect.helper

import org.n1.av2.run.entity.NodeScanStatus
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class NodeAccessHelper(
    private val runEntityService: RunEntityService,
    private val nodeEntityService: NodeEntityService,
) {

    fun checkNodeRevealed(networkId:String, runId: String): String? {
        val run = runEntityService.getByRunId(runId)
        val targetNode = nodeEntityService.findByNetworkId(run.siteId, networkId)?: return nodeNotFound(networkId)

        val targetNodeStatus = run.nodeScanById[targetNode.id]?.status ?: return nodeNotFound(networkId)
        if (targetNodeStatus == NodeScanStatus.UNDISCOVERED_0 || targetNodeStatus == NodeScanStatus.UNCONNECTABLE_1) {
            return nodeNotFound(networkId)
        }
        return null
    }

    private fun nodeNotFound(networkId: String) = "Node [ok]$networkId[/] not found."
}
