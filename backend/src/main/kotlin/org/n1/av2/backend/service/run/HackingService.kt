package org.n1.av2.backend.service.run

import mu.KLogging
import org.n1.av2.backend.model.db.layer.TimerTriggerLayer
import org.n1.av2.backend.model.db.run.NodeScanStatus
import org.n1.av2.backend.model.db.run.NodeScanStatus.*
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.IceStatusRepo
import org.n1.av2.backend.repo.LayerStatusRepo
import org.n1.av2.backend.repo.NodeStatusRepo
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layer.TimerTriggerLayerService
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.n1.av2.backend.service.scan.ScanProbeService
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.service.terminal.HackTerminalService
import org.n1.av2.backend.service.user.HackerActivityService
import org.springframework.stereotype.Service

val STATUSES_NEEDING_PROBE_LAYERS = listOf(DISCOVERED, TYPE, CONNECTIONS)

@Service
class HackingService(
        private val hackerPositionService: HackerPositionService,
        private val currentUserService: CurrentUserService,
        private val scanService: ScanService,
        private val nodeService: NodeService,
        private val probeService: ScanProbeService,
        private val userActivityService: HackerActivityService,
        private val hackTerminalService: HackTerminalService,
        private val alarmService: TimerTriggerLayerService,
        private val layerStatusRepo: LayerStatusRepo,
        private val nodeStatusRepo: NodeStatusRepo,
        private val iceStatusRepo: IceStatusRepo,
        private val tracingPatrollerService: TracingPatrollerService,
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

    private fun triggerLayersAtArrive(nodeId: String, userId: String, runId: String) {
        val node = nodeService.getById(nodeId)
        node.layers.forEach { layer ->
            when (layer) {
                is TimerTriggerLayer -> alarmService.hackerTriggers(layer, nodeId, userId, runId)
                else -> { } // do nothing
            }
        }
    }

    fun probedLayers(nodeId: String, runId: String) {
        val scan = scanService.getByRunId(runId)
        val nodeScan = scan.nodeScanById[nodeId]!!

        val newNodeStatus = when (nodeScan.status) {
            DISCOVERED, NodeScanStatus.TYPE -> NodeScanStatus.LAYERS_NO_CONNECTIONS
            CONNECTIONS -> NodeScanStatus.LAYERS
            else -> nodeScan.status
        }
        if (newNodeStatus != nodeScan.status) {
            probeService.probeScanSingleNode(nodeScan, scan, nodeId, newNodeStatus)
        }

        val data = MoveArrive(nodeId, currentUserService.userId)
        val position = hackerPositionService.retrieveForCurrentUser()
        hackerPositionService.arriveAt(position, nodeId)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_ARRIVE, data)
    }

    fun probedConnections(nodeId: String, runId: String) {
        val scan = scanService.getByRunId(runId)
        val nodeScan = scan.nodeScanById[nodeId]!!
        val node = nodeService.getById(nodeId)
        val layer = node.layers.first()
        val prefix = "Hacked: [pri]0[/] ${layer.name}"
        probeService.probeScanConnection(scan, node, nodeScan, prefix)
    }

    fun purgeAll() {
        layerStatusRepo.deleteAll()
        iceStatusRepo.deleteAll()
        hackerPositionService.purgeAll()
        tracingPatrollerService.purgeAll()
    }

    fun reset() {
        layerStatusRepo.deleteAll()
        nodeStatusRepo.deleteAll()
        iceStatusRepo.deleteAll()
        tracingPatrollerService.purgeAll()
    }

}