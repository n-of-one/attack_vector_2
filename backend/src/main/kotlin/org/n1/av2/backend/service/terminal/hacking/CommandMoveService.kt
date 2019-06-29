package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.model.db.run.NodeStatus
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.service.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.HackerPositionService
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.ConnectionService
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service

@Service
class CommandMoveService(
        private val stompService: StompService,
        private val nodeService: NodeService,
        private val connectionService: ConnectionService,
        private val hackerPositionService: HackerPositionService,
        private val scanService: ScanService

        ) {

    fun process(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            stompService.terminalReceive("Missing [ok]<network id>[/], for example: [u]mv[ok] 01[/].")
            return
        }
        val networkId = tokens[1]
        val position = hackerPositionService.retrieve()

        if (position.inTransit) return reportInTransit()
        val toNode = nodeService.findByNetworkId(position.siteId, networkId) ?: return reportNodeNotFound(networkId)

        if (toNode.id == position.currentNodeId) return reportAtTargetNode(networkId)

        if (position.previousNodeId == toNode.id) return handleMove(runId, toNode, position)
        val scan = scanService.getByRunId(runId)
        if (scan.nodeScanById[toNode.id] == null || scan.nodeScanById[toNode.id]!!.status == NodeStatus.UNDISCOVERED) return reportNodeNotFound(networkId)
        connectionService.findConnection(position.currentNodeId, toNode.id) ?: return reportNoPath(networkId)

        val fromNode = nodeService.getById(position.currentNodeId)
        if (hasActiveIce(fromNode)) return reportProtected()

        handleMove(runId, toNode, position)
    }

    private fun reportAtTargetNode(networkId: String) {
        stompService.terminalReceive("[error]error[/] already at [ok]${networkId}[/].")
    }

    private fun reportInTransit() {
        stompService.terminalReceive("[error]busy[/] current move not finished.")
    }

    fun reportNodeNotFound(networkId: String) {
        stompService.terminalReceive("[error]error[/] node [ok]${networkId}[/] not found.")
    }

    fun reportNoPath(networkId: String) {
        stompService.terminalReceive("[error]error[/] no path from current node to [ok]${networkId}[/].")
    }

    private fun hasActiveIce(node: Node): Boolean {
        return node.services.any { it.type.ice  && !(it.hacked) }
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