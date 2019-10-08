package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.engine.GameEvent
import org.n1.av2.backend.engine.TimedEventQueue
import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.model.db.run.NodeScanStatus
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.NodeStatusRepo
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.HackerPositionService
import org.n1.av2.backend.service.run.STATUSES_NEEDING_PROBE_LAYERS
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.ConnectionService
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service

private class Ticks(val start: Int = TICKS_HACKER_MOVE_START, val main: Int = TICKS_HACKER_MOVE_MAIN)
private val TICKS = Ticks()

private class MoveArriveGameEvent(val nodeId: String, val userId: String, val runId: String, val ticks: Ticks = TICKS): GameEvent

const val TICKS_HACKER_MOVE_MAIN = 16
const val TICKS_HACKER_MOVE_START = 4
const val TICKS_HACKER_MOVE_END = 4

@Service
class CommandMoveService(
        private val nodeService: NodeService,
        private val connectionService: ConnectionService,
        private val hackerPositionService: HackerPositionService,
        private val scanService: ScanService,
        private val nodeStatusRepo: NodeStatusRepo,
        private val timedEventQueue: TimedEventQueue,
        private val stompService: StompService
) {

    fun process(runId: String, tokens: List<String>, position: HackerPosition) {
        if (tokens.size == 1) {
            stompService.terminalReceive("Missing [ok]<network id>[/], for example: [u]mv[ok] 01[/].")
            return
        }
        val networkId = tokens[1]

        val toNode = nodeService.findByNetworkId(position.siteId, networkId) ?: return reportNodeNotFound(networkId)

        if (toNode.id == position.currentNodeId) return reportAtTargetNode(networkId)

        if (position.previousNodeId == toNode.id) return handleMove(runId, toNode, position)
        val scan = scanService.getByRunId(runId)
        if (scan.nodeScanById[toNode.id] == null || scan.nodeScanById[toNode.id]!!.status == NodeScanStatus.UNDISCOVERED) return reportNodeNotFound(networkId)
        connectionService.findConnection(position.currentNodeId, toNode.id) ?: return reportNoPath(networkId)

        val fromNode = nodeService.getById(position.currentNodeId)
        if (hasActiveIce(fromNode, runId)) return reportProtected()


        handleMove(runId, toNode, position)
    }

    private fun reportAtTargetNode(networkId: String) {
        stompService.terminalReceive("[error]error[/] already at [ok]${networkId}[/].")
    }

    fun reportNodeNotFound(networkId: String) {
        stompService.terminalReceive("[error]error[/] node [ok]${networkId}[/] not found.")
    }

    fun reportNoPath(networkId: String) {
        stompService.terminalReceive("[error]error[/] no path from current node to [ok]${networkId}[/].")
    }

    private fun hasActiveIce(node: Node, runId: String): Boolean {
        if (node.layers.none{it.type.ice}) {
            return false
        }

        val nodeStatus = nodeStatusRepo.findByNodeIdAndRunId(node.id, runId)
        return (nodeStatus == null || !nodeStatus.hacked)
    }

    private fun reportProtected() {
        stompService.terminalReceive("[warn b]blocked[/] ICE in current node is blocking your move.")
    }

    private data class StartMove(val userId: String, val nodeId: String)

    private fun handleMove(runId: String, toNode: Node, position: HackerPosition) {
        hackerPositionService.saveInTransit(position, toNode.id)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_START, StartMove(position.userId, toNode.id))

        val moveArriveEvent = MoveArriveGameEvent(toNode.id, position.userId, runId)
        timedEventQueue.queueInTicks(TICKS_HACKER_MOVE_START + TICKS_HACKER_MOVE_MAIN, moveArriveEvent)
    }



    private data class MoveArrive(val nodeId: String, val userId: String)

    fun moveArrive(nodeId: String, runId: String) {
        val position = hackerPositionService.retrieveForCurrentUser()
        if (position.locked) {

            class ActionSnapBack(val hackerId: String, val nodeId: String)
            stompService.toRun(runId, ReduxActions.SERVER_PATROLLER_SNAPS_BACK_HACKER, ActionSnapBack(position.userId, position.currentNodeId))
            return
        }


        val scan = scanService.getByRunId(runId)
        val nodeStatus = scan.nodeScanById[nodeId]!!.status

        val userId = currentUserService.userId

        val data = MoveArrive(nodeId, userId)
        if (STATUSES_NEEDING_PROBE_LAYERS.contains(nodeStatus)) {
            stompService.toRun(runId, ReduxActions.SERVER_HACKER_PROBE_LAYERS, data)
        } else {
            hackerPositionService.arriveAt(position, nodeId)
            triggerLayersAtArrive(nodeId, userId, runId)
            stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_ARRIVE, data)
        }
    }





}