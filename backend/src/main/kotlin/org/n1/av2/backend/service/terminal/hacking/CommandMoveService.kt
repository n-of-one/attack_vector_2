package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.model.db.run.NodeStatus
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.HackerPositionService
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.ConnectionService
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service

@Service
class CommandMoveService(
        val stompService: StompService,
        val nodeService: NodeService,
        val connectionService: ConnectionService,
        val currentUserService: CurrentUserService,
        val hackerPositionService: HackerPositionService,
        val scanService: ScanService

        ) {

    fun process(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            stompService.terminalReceive("Missing [ok]<network id>[/], for example: [u]mv[ok] 01[/].")
            return
        }
        val networkId = tokens[1]
        val userId = currentUserService.userId
        val position = hackerPositionService.getByRunIdAndUserId(runId, userId)
        val toNode = nodeService.findByNetworkId(position.siteId, networkId) ?: return reportNodeNotFound()
        if (position.previousNodeId == toNode.id) return handleMove(runId, toNode, position)
        val scan = scanService.getByRunId(runId)
        if (scan.nodeScanById[toNode.id] == null || scan.nodeScanById[toNode.id]!!.status == NodeStatus.UNDISCOVERED) return reportNodeNotFound()
        connectionService.findConnection(position.currentNodeId, toNode.id) ?: return reportNoPath(networkId)

        val fromNode = nodeService.getById(position.currentNodeId)
        if (nodeService.hasActiveIce(fromNode)) return reportProtected()

        handleMove(runId, toNode, position)
    }

    private fun reportProtected() {
        stompService.terminalReceive("[warn]blocked[/] ICE in current node is blocking your move.")
    }

    fun reportNodeNotFound() {
        stompService.terminalReceive("[warn]error[/] node not found.")
    }

    fun reportNoPath(networkId: String) {
        stompService.terminalReceive("[warn]error[/] no path from current node to [ok]${networkId}[/].")
    }

    private data class StartMove(val userId: String, val nodeId: String)
    private fun handleMove(runId: String, toNode: Node, position: HackerPosition) {
        hackerPositionService.saveInTransit(position)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_START_MOVE, StartMove(position.userId, toNode.id))
    }
}