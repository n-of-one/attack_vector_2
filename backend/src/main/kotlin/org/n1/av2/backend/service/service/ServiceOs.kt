package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.model.db.run.NodeScanStatus
import org.n1.av2.backend.model.db.service.Service
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.scan.ScanService



@org.springframework.stereotype.Service
class ServiceOs(
        val scanService: ScanService,
        val stompService: StompService) {

    private data class ProbeConnections(val nodeId: String, val userId: String)
    fun hack(service: Service, node: Node, position: HackerPosition) {
        val scan = scanService.getByRunId(position.runId)
        val nodeStatus = scan.nodeScanById[node.id]!!.status

        if (nodeStatus != NodeScanStatus.SERVICES) {
            val data = ProbeConnections(node.id, position.userId)
            stompService.toRun(position.runId, ReduxActions.SERVER_HACKER_PROBE_CONNECTIONS, data)
        }
        else {
            stompService.terminalReceive("Hacking ${service.name} reveals nothing new.")
        }
    }

}