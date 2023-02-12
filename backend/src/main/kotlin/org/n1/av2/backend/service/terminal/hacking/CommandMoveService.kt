package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.engine.TicksGameEvent
import org.n1.av2.backend.model.Ticks
import org.n1.av2.backend.model.db.layer.TimerTriggerLayer
import org.n1.av2.backend.model.db.run.HackerStateRunning
import org.n1.av2.backend.model.db.run.NodeScan
import org.n1.av2.backend.model.db.run.NodeScanStatus
import org.n1.av2.backend.model.db.run.Scan
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


private val MOVE_START_TICKS = Ticks("main" to 30)
private val MOVE_ARRIVE_TICKS = Ticks("main" to 8)

class MoveArriveGameEvent(val nodeId: String, val userId: String, val runId: String, ticks: Ticks = MOVE_ARRIVE_TICKS) : TicksGameEvent(ticks)

private val HACKER_SCANS_NODE_TICKS = Ticks("in" to 50, "out" to 25)

class HackerScannedNodeEvent(val nodeId: String, val userId: String, val runId: String)


@Service
class CommandMoveService(
    private val nodeService: NodeService,
    private val connectionService: ConnectionService,
    private val hackerStateService: HackerStateService,
    private val scanService: ScanService,
    private val nodeStatusRepo: NodeStatusRepo,
    private val timerTriggerLayerService: TimerTriggerLayerService,
    private val probeService: ScanProbeService,
    private val tracingPatrollerService: TracingPatrollerService,
    private val taskRunner: TaskRunner,
    private val stompService: StompService
) {

    fun processCommand(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (tokens.size == 1) {
            stompService.terminalReceiveCurrentUser("Missing [ok]<network id>[/], for example: [u]mv[ok] 01[/].")
            return
        }
        val networkId = tokens[1]
        val toNode = nodeService.findByNetworkId(state.siteId, networkId) ?: return reportNodeNotFound(networkId)

        if (checkMovePrerequisites(toNode, state, networkId, runId)) {
            handleMove(runId, toNode, state)
        }
    }

    private fun checkMovePrerequisites(toNode: Node, state: HackerStateRunning, networkId: String, runId: String): Boolean {
        if (toNode.id == state.currentNodeId) return reportAtTargetNode(networkId)

        if (state.previousNodeId == toNode.id) return true /// can always go back to previous node, regardless of ice

        val scan = scanService.getByRunId(runId)
        if (scan.nodeScanById[toNode.id] == null || scan.nodeScanById[toNode.id]!!.status == NodeScanStatus.UNDISCOVERED_0) {
            reportNodeNotFound(networkId)
            return false
        }

        connectionService.findConnection(state.currentNodeId, toNode.id) ?: return reportNoPath(networkId)
        val fromNode = nodeService.getById(state.currentNodeId)
        if (hasActiveIce(fromNode, runId)) return reportProtected()

        return true
    }

    private fun reportAtTargetNode(networkId: String): Boolean {
        stompService.terminalReceiveCurrentUser("[error]error[/] already at [ok]${networkId}[/].")
        return false
    }

    fun reportNodeNotFound(networkId: String) {
        stompService.terminalReceiveCurrentUser("[error]error[/] node [ok]${networkId}[/] not found.")
    }

    fun reportNoPath(networkId: String): Boolean {
        stompService.terminalReceiveCurrentUser("[error]error[/] no path from current node to [ok]${networkId}[/].")
        return false
    }

    private fun hasActiveIce(node: Node, runId: String): Boolean {
        if (node.layers.none { it.type.ice }) {
            return false
        }

        val nodeStatus = nodeStatusRepo.findByNodeIdAndRunId(node.id, runId)
        return (nodeStatus == null || !nodeStatus.hacked)
    }

    private fun reportProtected(): Boolean {
        stompService.terminalReceiveCurrentUser("[warn b]blocked[/] ICE in current node is blocking your move.")
        return false
    }


    private fun handleMove(runId: String, toNode: Node, state: HackerStateRunning) {
        stompService.terminalSetLockedCurrentUser(true)
        class StartMove(val userId: String, val nodeId: String, val ticks: Ticks)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_START, StartMove(state.userId, toNode.id, MOVE_START_TICKS))

        val moveArriveEvent = MoveArriveGameEvent(toNode.id, state.userId, runId)
        taskRunner.queueInTicks(MOVE_START_TICKS.total) { moveArrive(moveArriveEvent) }
    }

    fun moveArrive(arriveEvent: MoveArriveGameEvent) {
        val runId = arriveEvent.runId
        val userId = arriveEvent.userId
        val nodeId = arriveEvent.nodeId

        val state = hackerStateService.retrieve(userId).toRunState()

        if (state.locked) {
            class MoveArriveFailAction(val userId: String)
            stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_ARRIVE_FAIL, MoveArriveFailAction(userId))
            return
        }

        val scan = scanService.getByRunId(runId)
        val nodeStatus = scan.nodeScanById[nodeId]!!.status

        if (nodeStatus == NodeScanStatus.FULLY_SCANNED_4) {
            arriveComplete(state, nodeId, userId, runId)
        } else {
            class HackerScansNodeAction(val userId: String, val nodeId: String, val ticks: Ticks = HACKER_SCANS_NODE_TICKS)

            val action = HackerScansNodeAction(userId, nodeId)
            stompService.toRun(runId, ReduxActions.SERVER_HACKER_SCANS_NODE, action)
            val event = HackerScannedNodeEvent(nodeId, userId, runId)
            taskRunner.queueInTicks(action.ticks.total) { hackerScannedNode(event) }
        }
    }

    fun hackerScannedNode(event: HackerScannedNodeEvent) {
        val runId = event.runId
        val userId = event.userId
        val nodeId = event.nodeId

        val scan = scanService.getByRunId(runId)
        val nodeScan = scan.nodeScanById[nodeId]!!

        when (nodeScan.status) {
            NodeScanStatus.DISCOVERED_1 -> hackerScannedNodeAndConnections(scan, nodeId, nodeScan)
            NodeScanStatus.TYPE_KNOWN_2 -> hackerScannedNodeAndConnections(scan, nodeId, nodeScan)
            NodeScanStatus.CONNECTIONS_KNOWN_3 -> probeService.scannedSingleNode(nodeScan, scan, nodeId, NodeScanStatus.FULLY_SCANNED_4)
            NodeScanStatus.FULLY_SCANNED_4 -> return
            else -> error("Unexpected status: " + nodeScan.status)
        }

        val state = hackerStateService.retrieve(userId).toRunState()
        arriveComplete(state, nodeId, userId, runId)
    }

    private fun hackerScannedNodeAndConnections(scan: Scan, nodeId: String, nodeScan: NodeScan) {
        val node = nodeService.getById(nodeId)
        probeService.scannedConnections(scan, node, nodeScan, false)
    }


    private fun arriveComplete(state: HackerStateRunning, nodeId: String, userId: String, runId: String) {
        hackerStateService.arriveAt(state, nodeId)
        triggerLayersAtArrive(nodeId, userId, runId)

        class MoveArriveMessage(val nodeId: String, val userId: String, val ticks: Ticks)

        val data = MoveArriveMessage(nodeId, userId, MOVE_ARRIVE_TICKS)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_ARRIVE, data)
        stompService.terminalSetLockedCurrentUser(false)
    }


    private fun triggerLayersAtArrive(nodeId: String, userId: String, runId: String) {
        val node = nodeService.getById(nodeId)
        node.layers.forEach { layer ->
            when (layer) {
                is TimerTriggerLayer -> timerTriggerLayerService.hackerTriggers(layer, nodeId, userId, runId)
                else -> {} // do nothing
            }
        }
    }


}