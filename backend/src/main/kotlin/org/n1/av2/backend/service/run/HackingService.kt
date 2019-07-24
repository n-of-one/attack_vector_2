package org.n1.av2.backend.service.run

import mu.KLogging
import org.n1.av2.backend.model.db.run.NodeStatus
import org.n1.av2.backend.model.db.run.NodeStatus.*
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.scan.ScanProbeService
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.service.terminal.HackTerminalService
import org.n1.av2.backend.service.user.HackerActivityService
import org.springframework.stereotype.Service

val STATUSES_NEEDING_PROBE_SERVICES = listOf( DISCOVERED, TYPE, CONNECTIONS)

@Service
class HackingService(
        private val hackerPositionService: HackerPositionService,
        private val currentUserService: CurrentUserService,
        private val scanService: ScanService,
        private val nodeService: NodeService,
        private val probeService: ScanProbeService,
        private val userActivityService: HackerActivityService,
        private val hackTerminalService: HackTerminalService,
        private val stompService: StompService) {

    companion object : KLogging()


    data class StartRun(val userId: String, val quick: Boolean)

    fun startAttack(runId: String, quick: Boolean) {
        userActivityService.startActivityHacking(runId)
        hackTerminalService.sendSyntaxHighlighting()
        hackerPositionService.startRun(runId)
        val data = StartRun(currentUserService.userId, quick)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_START_ATTACK, data)
    }

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
        val service = node.services.first()
        val prefix = "Hacked: [pri]0[/] ${service.name}"
        probeService.probeScanConnection(scan, node, nodeScan, prefix)
    }

}