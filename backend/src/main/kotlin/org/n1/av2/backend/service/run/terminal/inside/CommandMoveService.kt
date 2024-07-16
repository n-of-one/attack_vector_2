package org.n1.av2.backend.service.run.terminal.inside

import org.n1.av2.backend.engine.ScheduledTask
import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.run.HackerStateRunning
import org.n1.av2.backend.entity.run.NodeScanStatus.*
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.ConnectionEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.entity.site.layer.other.TripwireLayer
import org.n1.av2.backend.model.Timings
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.layerhacking.service.TripwireLayerService
import org.n1.av2.backend.service.run.terminal.scanning.ScanService
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.util.isOneOf
import org.springframework.stereotype.Service


private val MOVE_START_Timings = Timings("main" to 30)
private val MOVE_ARRIVE_Timings = Timings("main" to 8)

val HACKER_SCANS_NODE_Timings = Timings("in" to 50, "out" to 25)


@Service
class CommandMoveService(
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val runEntityService: RunEntityService,
    private val tripwireLayerService: TripwireLayerService,
    private val scanService: ScanService,
    private val userTaskRunner: UserTaskRunner,
    private val stompService: StompService
) {

    fun processCommand(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (tokens.size == 1) {
            stompService.replyTerminalReceive("Missing [ok]<network id>[/], for example: [b]mv[ok] 01[/].")
            return
        }
        val networkId = tokens[1]
        val toNode = nodeEntityService.findByNetworkId(state.siteId, networkId) ?: return reportNodeNotFound(networkId)

        if (checkMovePrerequisites(toNode, state, networkId, runId)) {
            handleMove(runId, toNode, state)
        }
    }

    private fun checkMovePrerequisites(toNode: Node, state: HackerStateRunning, networkId: String, runId: String): Boolean {
        if (toNode.id == state.currentNodeId) return reportAtTargetNode(networkId)

        if (state.previousNodeId == toNode.id) return true /// can always go back to previous node, regardless of ice

        val scan = runEntityService.getByRunId(runId)
        if (scan.nodeScanById[toNode.id] == null || scan.nodeScanById[toNode.id]!!.status.isOneOf(UNDISCOVERED_0, UNCONNECTABLE_1)) {
            reportNodeNotFound(networkId)
            return false
        }

        connectionEntityService.findConnection(state.currentNodeId, toNode.id) ?: return reportNoPath(networkId)
        val fromNode = nodeEntityService.getById(state.currentNodeId)
        if (hasActiveIce(fromNode)) return reportProtected()

        return true
    }

    private fun reportAtTargetNode(networkId: String): Boolean {
        stompService.replyTerminalReceive("[error]error[/] already at [ok]${networkId}[/].")
        return false
    }

    fun reportNodeNotFound(networkId: String) {
        stompService.replyTerminalReceive("[error]error[/] node [ok]${networkId}[/] not found.")
    }

    fun reportNoPath(networkId: String): Boolean {
        stompService.replyTerminalReceive("[error]error[/] no path from current node to [ok]${networkId}[/].")
        return false
    }

    private fun hasActiveIce(node: Node): Boolean {
        return node.layers.any { it is IceLayer && !it.hacked }

    }

    private fun reportProtected(): Boolean {
        stompService.replyTerminalReceive("[warn b]blocked[/] ICE in current node is blocking your move.")
        return false
    }

    private fun handleMove(runId: String, toNode: Node, state: HackerStateRunning) {
        stompService.replyTerminalSetLocked(true)
        class StartMove(val userId: String, val nodeId: String, val timings: Timings)
        stompService.toRun(runId, ServerActions.SERVER_HACKER_MOVE_START, StartMove(state.userId, toNode.id, MOVE_START_Timings))

        userTaskRunner.queueInTicksForSite("move-arrive", state.siteId, MOVE_START_Timings.totalTicks) { moveArrive(toNode.id, state.userId, runId) }
    }

    @ScheduledTask
    fun moveArrive(nodeId: String, userId: String, runId: String ) {
        val state = hackerStateEntityService.retrieve(userId).toRunState()

        val run = runEntityService.getByRunId(runId)
        val nodeStatus = run.nodeScanById[nodeId]!!.status

        if (nodeStatus.isOneOf(FULLY_SCANNED_4, ICE_PROTECTED_3)) {
            arriveComplete(nodeId, userId, runId)
        } else {
            stompService.toRun(runId, ServerActions.SERVER_HACKER_SCANS_NODE, "userId" to userId, "nodeId" to nodeId, "timings" to HACKER_SCANS_NODE_Timings)

            userTaskRunner.queueInTicksForSite("internal-scan", state.siteId, HACKER_SCANS_NODE_Timings.totalTicks) {
                scanService.hackerArrivedNodeScan(nodeId, userId, runId)
                arriveComplete(nodeId, userId, runId)
            }
        }
    }

    private fun arriveComplete(nodeId: String, userId: String, runId: String) {
        val state = hackerStateEntityService.retrieve(userId).toRunState()
        hackerStateEntityService.arriveAt(state, nodeId)
        triggerLayersAtArrive(state.siteId, nodeId, userId, runId)

        stompService.toRun(
            runId, ServerActions.SERVER_HACKER_MOVE_ARRIVE,
            "nodeId" to nodeId, "userId" to userId, "timings" to MOVE_ARRIVE_Timings
        )
        stompService.replyTerminalSetLocked(false)
    }

    private fun triggerLayersAtArrive(siteId: String, nodeId: String, userId: String, runId: String) {
        val node = nodeEntityService.getById(nodeId)
        node.layers.forEach { layer ->
            when (layer) {
                is TripwireLayer -> tripwireLayerService.hackerArrivesNode(siteId, layer, nodeId, userId, runId)
                else -> {} // do nothing
            }
        }
    }
}
