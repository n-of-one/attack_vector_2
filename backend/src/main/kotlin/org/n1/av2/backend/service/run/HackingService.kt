package org.n1.av2.backend.service.run

import mu.KLogging
import org.n1.av2.backend.model.db.run.NodeScanStatus
import org.n1.av2.backend.model.db.run.NodeScanStatus.*
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.IcePasswordStatusRepo
import org.n1.av2.backend.repo.LayerStatusRepo
import org.n1.av2.backend.repo.NodeStatusRepo
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.scan.ScanProbeService
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.service.terminal.HackTerminalService
import org.n1.av2.backend.service.user.HackerActivityService
import org.springframework.stereotype.Service

val STATUSES_NEEDING_PROBE_LAYERS = listOf( DISCOVERED, TYPE, CONNECTIONS)

@Service
class HackingService(
        private val hackerPositionService: HackerPositionService,
        private val currentUserService: CurrentUserService,
        private val scanService: ScanService,
        private val nodeService: NodeService,
        private val probeService: ScanProbeService,
        private val userActivityService: HackerActivityService,
        private val hackTerminalService: HackTerminalService,
        private val stompService: StompService,
        private val layerStatusRepo: LayerStatusRepo,
        private val nodeStatusRepo: NodeStatusRepo,
        private val icePasswordStatusRepo: IcePasswordStatusRepo) {

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
        if (STATUSES_NEEDING_PROBE_LAYERS.contains(nodeStatus)) {
            stompService.toRun(runId, ReduxActions.SERVER_HACKER_PROBE_LAYERS, data)
        }
        else {
            hackerPositionService.arriveAt(nodeId)
            stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_ARRIVE, data)
        }
    }

    fun probedLayers(nodeId: String, runId: String) {
        val scan = scanService.getByRunId(runId)
        val nodeScan= scan.nodeScanById[nodeId]!!

        val newNodeStatus = when (nodeScan.status) {
            DISCOVERED, NodeScanStatus.TYPE -> NodeScanStatus.SERVICES_NO_CONNECTIONS
            CONNECTIONS -> NodeScanStatus.LAYERS
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
        val layer = node.layers.first()
        val prefix = "Hacked: [pri]0[/] ${layer.name}"
        probeService.probeScanConnection(scan, node, nodeScan, prefix)
    }

    fun purgeAll() {
        layerStatusRepo.deleteAll()
        icePasswordStatusRepo.deleteAll()
        hackerPositionService.purgeAll()
    }

    fun reset() {
        layerStatusRepo.deleteAll()
        nodeStatusRepo.deleteAll()
        icePasswordStatusRepo.deleteAll()
    }

}