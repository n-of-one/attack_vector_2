package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.engine.TicksGameEvent
import org.n1.av2.backend.engine.TimedEventQueue
import org.n1.av2.backend.model.Ticks
import org.n1.av2.backend.model.db.layer.TimerTriggerLayer
import org.n1.av2.backend.model.db.run.HackerStateRunning
import org.n1.av2.backend.model.db.run.NodeScanStatus
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.NodeStatusRepo
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layer.TimerTriggerLayerService
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.n1.av2.backend.service.run.HackerStateService
import org.n1.av2.backend.service.scan.ScanProbeService
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.ConnectionService
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service

private val STATUSES_NEEDING_PROBE_LAYERS = listOf(NodeScanStatus.DISCOVERED, NodeScanStatus.TYPE, NodeScanStatus.CONNECTIONS)


private val MOVE_START_TICKS = Ticks("start" to 4, "main" to 80)
class MoveArriveGameEvent(val nodeId: String, val userId: String, val runId: String, ticks: Ticks = MOVE_START_TICKS): TicksGameEvent(ticks)

private val PROBE_LAYERS_TICKS = Ticks("start" to 50, "end" to 50)
class ArriveProbeLayersGameEvent(val nodeId: String, val userId: String, val runId: String, ticks: Ticks): TicksGameEvent(ticks)


@Service
class CommandMoveService(
        private val nodeService: NodeService,
        private val connectionService: ConnectionService,
        private val hackerStateService: HackerStateService,
        private val scanService: ScanService,
        private val nodeStatusRepo: NodeStatusRepo,
        private val timedEventQueue: TimedEventQueue,
        private val timerTriggerLayerService: TimerTriggerLayerService,
        private val probeService: ScanProbeService,
        private val tracingPatrollerService: TracingPatrollerService,
        private val stompService: StompService) {

    fun processCommand(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (tokens.size == 1) {
            stompService.terminalReceiveCurrentUser("Missing [ok]<network id>[/], for example: [u]mv[ok] 01[/].")
            return
        }
        val networkId = tokens[1]

        val toNode = nodeService.findByNetworkId(state.siteId, networkId) ?: return reportNodeNotFound(networkId)

        if (toNode.id == state.currentNodeId) return reportAtTargetNode(networkId)

        if (state.previousNodeId == toNode.id) return handleMove(runId, toNode, state)
        val scan = scanService.getByRunId(runId)
        if (scan.nodeScanById[toNode.id] == null || scan.nodeScanById[toNode.id]!!.status == NodeScanStatus.UNDISCOVERED) return reportNodeNotFound(networkId)
        connectionService.findConnection(state.currentNodeId, toNode.id) ?: return reportNoPath(networkId)

        val fromNode = nodeService.getById(state.currentNodeId)
        if (hasActiveIce(fromNode, runId)) return reportProtected()


        handleMove(runId, toNode, state)
    }

    private fun reportAtTargetNode(networkId: String) {
        stompService.terminalReceiveCurrentUser("[error]error[/] already at [ok]${networkId}[/].")
    }

    fun reportNodeNotFound(networkId: String) {
        stompService.terminalReceiveCurrentUser("[error]error[/] node [ok]${networkId}[/] not found.")
    }

    fun reportNoPath(networkId: String) {
        stompService.terminalReceiveCurrentUser("[error]error[/] no path from current node to [ok]${networkId}[/].")
    }

    private fun hasActiveIce(node: Node, runId: String): Boolean {
        if (node.layers.none{it.type.ice}) {
            return false
        }

        val nodeStatus = nodeStatusRepo.findByNodeIdAndRunId(node.id, runId)
        return (nodeStatus == null || !nodeStatus.hacked)
    }

    private fun reportProtected() {
        stompService.terminalReceiveCurrentUser("[warn b]blocked[/] ICE in current node is blocking your move.")
    }


    private fun handleMove(runId: String, toNode: Node, state: HackerStateRunning) {
        hackerStateService.saveInTransit(state, toNode.id)

        class StartMove(val userId: String, val nodeId: String, val ticks: Ticks)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_START, StartMove(state.userId, toNode.id, MOVE_START_TICKS))

        val moveArriveEvent = MoveArriveGameEvent(toNode.id, state.userId, runId)
        timedEventQueue.queueInTicks(moveArriveEvent)
    }

    fun moveArrive(event: MoveArriveGameEvent) {
        val runId = event.runId
        val userId = event.userId
        val nodeId = event.nodeId

        val state = hackerStateService.retrieve(userId).toRunState()
        if (state.hookPatrollerId != null) {
            tracingPatrollerService.snapBack(state, event)
            return
        }

        val scan = scanService.getByRunId(runId)
        val nodeStatus = scan.nodeScanById[nodeId]!!.status


        if (STATUSES_NEEDING_PROBE_LAYERS.contains(nodeStatus)) {
            val probeEvent = ArriveProbeLayersGameEvent(nodeId, userId, runId, PROBE_LAYERS_TICKS)
            timedEventQueue.queueInTicks(probeEvent)

            class ProbeLayers(val userId: String, ticks: Ticks)
            val data = ProbeLayers(userId, PROBE_LAYERS_TICKS)
            stompService.toRun(runId, ReduxActions.SERVER_HACKER_PROBE_LAYERS, data)
        } else {
            arriveComplete(state, nodeId, userId, runId)
        }
    }


    fun probedLayers(event: ArriveProbeLayersGameEvent) {
        val runId = event.runId
        val userId = event.userId
        val nodeId = event.nodeId

        val scan = scanService.getByRunId(runId)
        val nodeScan = scan.nodeScanById[nodeId]!!

        val newNodeStatus = when (nodeScan.status) {
            NodeScanStatus.DISCOVERED, NodeScanStatus.TYPE -> NodeScanStatus.LAYERS_NO_CONNECTIONS
            NodeScanStatus.CONNECTIONS -> NodeScanStatus.LAYERS
            else -> nodeScan.status
        }
        if (newNodeStatus != nodeScan.status) {
            probeService.probeScanSingleNode(nodeScan, scan, nodeId, newNodeStatus)
        }

        val state = hackerStateService.retrieve(userId).toRunState()
        arriveComplete(state, nodeId, userId, runId)
    }


    private fun arriveComplete(state: HackerStateRunning, nodeId: String, userId: String, runId: String) {
        hackerStateService.arriveAt(state, nodeId)
        triggerLayersAtArrive(nodeId, userId, runId)

        class MoveArrive(val nodeId: String, val userId: String)
        val data = MoveArrive(nodeId, userId)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_ARRIVE, data)
    }


    private fun triggerLayersAtArrive(nodeId: String, userId: String, runId: String) {
        val node = nodeService.getById(nodeId)
        node.layers.forEach { layer ->
            when (layer) {
                is TimerTriggerLayer -> timerTriggerLayerService.hackerTriggers(layer, nodeId, userId, runId)
                else -> { } // do nothing
            }
        }
    }


}