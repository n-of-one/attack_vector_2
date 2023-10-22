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

    fun performManualScan(run: Run, node: Node, status: NodeScanStatus) {
        val traverseNodesById = traverseNodeService.createTraverseNodesWithDistance(run)
        val targetTraverseNode: TraverseNode = traverseNodesById[node.id]!!
        launchProbe(targetTraverseNode, run)
    }

    data class ProbeAction(val probeUserId: String, val path: List<String>, val scanType: NodeScanType, val timings: Timings)

    private fun launchProbe(targetNode: TraverseNode, run: Run) {
        val status = run.nodeScanById[targetNode.id]!!.status
        val scanType = determineNodeScanType(status)
        val path = createNodePath(targetNode)
        val userId = currentUserService.userId
        val node = nodeEntityService.findById(targetNode.id)

        val timings = scanType.timings
        val probeAction = ProbeAction(userId, path, scanType, timings)

        stompService.toRun(run.runId, ServerActions.SERVER_PROBE_LAUNCH, probeAction)

        val totalTicks = (path.size) * timings.connection + timings.totalWithoutConnection
        userTaskRunner.queueInTicks(totalTicks - 20) {
            scanProbActionService.probeCompleted(run, node, scanType)
        }
    }

    fun determineNodeScanType(status: NodeScanStatus): NodeScanType {
        return when (status) {
            NodeScanStatus.DISCOVERED_1 -> NodeScanType.SCAN_NODE_INITIAL
            NodeScanStatus.TYPE_KNOWN_2 -> NodeScanType.SCAN_CONNECTIONS
            NodeScanStatus.CONNECTIONS_KNOWN_3 -> NodeScanType.SCAN_NODE_DEEP
            NodeScanStatus.FULLY_SCANNED_4 -> NodeScanType.SCAN_NODE_DEEP
            NodeScanStatus.UNDISCOVERED_0 -> error("Cannot scan a node that has not yet been discovered.")
        }
    }

    fun createNodePath(targetNode: TraverseNode): LinkedList<String> {
        val path = LinkedList<String>()
        path.add(targetNode.id)
        var currentNode = targetNode
        while (currentNode.distance != 1) {
            currentNode = currentNode.connections.find { it.distance == (currentNode.distance!! - 1) }!!
            path.add(0, currentNode.id)
        }
        return path
    }

    /**
     * Of all nodes that are know to the players, find the one of which the least is known (lowest scan status level).
     */
    private fun findProbeTarget(run: Run): TraverseNode? {
        val traverseNodeValues = traverseNodeService.createTraverseNodesWithDistance(run).values
        val traverseNodes = traverseNodeValues.filter { run.nodeScanById[it.id]!!.status != NodeScanStatus.UNDISCOVERED_0 }
        val distanceSortedNodes = traverseNodes.sortedBy { it.distance }
        val targetNode = distanceSortedNodes.minBy {
            run.nodeScanById[it.id]!!.status.level
        }
        return if (run.nodeScanById[targetNode.id]!!.status == NodeScanStatus.FULLY_SCANNED_4) {
            null
        } else targetNode
    }


    private fun allNodesScanned(run: Run): Boolean {
        return run.nodeScanById
            .filter { it.value.status !== NodeScanStatus.FULLY_SCANNED_4 }
            .isEmpty()
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

        stompService.toRun(run.runId, ServerActions.SERVER_DISCOVER_NODES, ProbeResultConnections(nodeStatusById, layout.connectionIds))
        scanInfoService.updateScanInfoToPlayers(run)
    }


}