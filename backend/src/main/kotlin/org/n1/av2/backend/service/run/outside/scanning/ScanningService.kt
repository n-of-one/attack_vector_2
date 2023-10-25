package org.n1.av2.backend.service.run.outside.scanning

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.entity.run.NodeScan
import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.LayoutEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.model.Timings
import org.n1.av2.backend.model.ui.NodeScanType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.site.ScanInfoService
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service
import java.util.*

@Service
class ScanningService(
    private val runEntityService: RunEntityService,
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val currentUserService: CurrentUserService,
    private val traverseNodeService: TraverseNodeService,
    private val userTaskRunner: UserTaskRunner,
    private val scanInfoService: ScanInfoService,
    private val scanProbActionService: ScanProbActionService,
    private val layoutEntityService: LayoutEntityService,
) {

    private val logger = mu.KotlinLogging.logger {}

    fun performManualScan(run: Run, node: Node) {
        val nodes = nodeEntityService.getAll(run.siteId)
        val traverseNodesById = traverseNodeService.createTraverseNodesWithDistance(run, nodes)
        val targetTraverseNode: TraverseNode = traverseNodesById[node.id]!!
        launchProbe(targetTraverseNode, run, nodes)
    }

    data class ProbeAction(val probeUserId: String, val path: List<String>, val scanType: NodeScanType, val timings: Timings)

    private fun launchProbe(targetNode: TraverseNode, run: Run, nodes: List<Node>) {
        val path = createNodePath(targetNode)

        if (pathCrossesIce(path, nodes)) {
            stompService.replyTerminalReceiveAndLocked(false, "Scan blocked by ICE at [ok]${targetNode.networkId}")
            return
        }

        val node = nodeEntityService.findById(targetNode.id)

        val timings = NodeScanType.SCAN_CONNECTIONS.timings
        val probeAction = ProbeAction(currentUserService.userId, path, NodeScanType.SCAN_CONNECTIONS, timings)

        stompService.toRun(run.runId, ServerActions.SERVER_PROBE_LAUNCH, probeAction)

        val totalTicks = (path.size) * timings.connection + timings.totalWithoutConnection
        userTaskRunner.queueInTicks(totalTicks - 20) {
            scanProbActionService.probeCompleted(run, node)
        }
    }

    fun createNodePath(targetNode: TraverseNode): List<String> {
        val path = LinkedList<String>()
        path.add(targetNode.id)
        var currentNode = targetNode
        while (currentNode.distance != 1) {
            currentNode = currentNode.connections.find { it.distance == (currentNode.distance!! - 1) }!!
            path.add(0, currentNode.id)
        }
        return path
    }

    private fun pathCrossesIce(path: List<String>, nodes: List<Node>): Boolean {
        if (path.size == 1) return false

        path.drop(1).forEach { nodeId ->
            val node = nodes.find { it.id === nodeId }!!
            if (node.ice && !node.hacked)
                return true
        }
        return false
    }


    fun quickScan(runId: String) {
        val run = runEntityService.getByRunId(runId)

        val layout = layoutEntityService.getBySiteId(run.siteId)

        layout.nodeIds.forEach {
            val oldStatus = run.nodeScanById[it]
            run.nodeScanById[it] = NodeScan(NodeScanStatus.FULLY_SCANNED_4, oldStatus!!.distance)
        }
        runEntityService.save(run)

        val nodeStatusById = layout.nodeIds.map {
            it to NodeScanStatus.FULLY_SCANNED_4
        }.toMap()

        stompService.toRun(run.runId, ServerActions.SERVER_DISCOVER_NODES, "nodeStatusById" to nodeStatusById)
        scanInfoService.updateScanInfoToPlayers(run)
    }


}