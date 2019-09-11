package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.model.db.run.NodeScanStatus
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.scan.ScanService


@org.springframework.stereotype.Service
class OsLayerService(
        val scanService: ScanService,
        val hackedUtil: HackedUtil,
        val stompService: StompService) {

    private data class ProbeConnections(val nodeId: String, val userId: String)
    fun hack(layer: Layer, node: Node, position: HackerPosition) {
        val scan = scanService.getByRunId(position.runId)
        val nodeStatus = scan.nodeScanById[node.id]!!.status

        if (nodeStatus != NodeScanStatus.LAYERS) {
            val data = ProbeConnections(node.id, position.userId)
            stompService.toRun(position.runId, ReduxActions.SERVER_HACKER_PROBE_CONNECTIONS, data)
            hackedUtil.nonIceHacked(layer.id, node, position.runId)
        }
        else {
            stompService.terminalReceive("Hacking ${layer.name} reveals nothing new.")
        }
    }

}