package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.engine.TimedEventQueue
import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.model.db.run.NodeScanStatus
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.NodeStatusRepo
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.run.AlarmGameEvent
import org.n1.av2.backend.service.run.HackerPositionService
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.ConnectionService
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class CommandMoveService(
        private val nodeService: NodeService,
        private val connectionService: ConnectionService,
        private val hackerPositionService: HackerPositionService,
        private val scanService: ScanService,
        private val nodeStatusRepo: NodeStatusRepo,
        private val stompService: StompService

        //fixme temporary to implement timer trigger client side
, private val time: TimeService
, private val timedEventQueue: TimedEventQueue

) {

    fun process(runId: String, tokens: List<String>, position: HackerPosition) {

        // fixme temporary to implement timer trigger client side
        class CountdownStart(val finishAt: ZonedDateTime)
        val alarmTime = time.now().plusSeconds(7)
        stompService.toRun(runId, ReduxActions.SERVER_START_COUNTDOWN, CountdownStart(alarmTime))
        timedEventQueue.queueInSeconds(7, AlarmGameEvent(runId, position.currentNodeId))

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
        if (position.locked) return reportLocked()
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
    fun reportLocked() {
        stompService.terminalReceive("[error]critical[/] OS refuses operation with error message [error]unauthorized[/].")
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
        hackerPositionService.saveInTransit(position)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_START, StartMove(position.userId, toNode.id))
    }
}