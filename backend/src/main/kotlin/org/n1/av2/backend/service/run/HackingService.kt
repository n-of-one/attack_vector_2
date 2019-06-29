package org.n1.av2.backend.service.run

import mu.KLogging
import org.n1.av2.backend.model.db.run.NodeStatus
import org.n1.av2.backend.model.db.run.NodeStatus.*
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.scan.ScanProbeService
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service

val STATUSES_NEEDING_PROBE_SERVICES = listOf( DISCOVERED, TYPE, CONNECTIONS)

@Service
class HackingService(
        val hackerPositionService: HackerPositionService,
        val currentUserService: CurrentUserService,
        val scanService: ScanService,
        val nodeService: NodeService,
        val probeService: ScanProbeService,
        val stompService: StompService) {

    companion object : KLogging()

    private data class MoveArrive(val nodeId: String, val userId: String)
    fun moveArrive(nodeId: String, runId: String) {
        val scan = scanService.getByRunId(runId)
        val nodeStatus = scan.nodeScanById[nodeId]!!.status

        val data = MoveArrive(nodeId, currentUserService.userId)
        if (STATUSES_NEEDING_PROBE_SERVICES.contains(nodeStatus)) {
            stompService.toRun(runId, ReduxActions.SERVER_HACKER_PROBE_SERVICES, data)
        }
        else {
            hackerPositionService.arriveAt(nodeId)
            stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_ARRIVE, data)
        }
    }

    fun probedServices(nodeId: String, runId: String) {
        val scan = scanService.getByRunId(runId)
        val nodeScan= scan.nodeScanById[nodeId]!!

        val newNodeStatus = when (nodeScan.status) {
            DISCOVERED, NodeStatus.TYPE -> NodeStatus.SERVICES_NO_CONNECTIONS
            CONNECTIONS -> NodeStatus.SERVICES
            else -> nodeScan.status
        }
        if (newNodeStatus != nodeScan.status) {
            probeService.probeScanSingleNode(nodeScan, scan, nodeId, newNodeStatus)
        }

        val data = MoveArrive(nodeId, currentUserService.userId)
        hackerPositionService.arriveAt(nodeId)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_ARRIVE, data)
    }

    fun probedConnections(nodeId: String, runId: String) {
        val scan = scanService.getByRunId(runId)
        val nodeScan= scan.nodeScanById[nodeId]!!
        val node = nodeService.getById(nodeId)
        probeService.probeScanConnection(scan, node, nodeScan)
    }

}