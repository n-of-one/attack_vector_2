package org.n1.av2.backend.service.run.terminal.scanning

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.entity.run.NodeScan
import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.LayoutEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.model.ui.NodeScanType.SCAN_CONNECTIONS
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.run.terminal.inside.HACKER_SCANS_NODE_Timings
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
    private val scanResultService: ScanResultService,
    private val layoutEntityService: LayoutEntityService,
) {

    private val logger = mu.KotlinLogging.logger {}

    fun scanFromOutside(run: Run, targetNode: Node) {
        val nodes = nodeEntityService.getAll(run.siteId)
        val traverseNodesById = traverseNodeService.createTraverseNodesWithDistance(run, nodes)
        val targetTraverseNode: TraverseNode = traverseNodesById[targetNode.id]!!
        val path = createNodePath(targetTraverseNode)

        if (pathCrossesIce(path, nodes)) {
            stompService.replyTerminalReceiveAndLocked(false, "Scan blocked by ICE at [ok]${targetTraverseNode.networkId}")
            return
        }

        val timings = SCAN_CONNECTIONS.timings

        stompService.toRun(run.runId, ServerActions.SERVER_PROBE_LAUNCH,
            "probeUserId" to currentUserService.userId  , "path" to path, "scanType" to SCAN_CONNECTIONS, "timings" to timings
        )

        val totalTicks = (path.size) * timings.connection + timings.totalWithoutConnection
        userTaskRunner.queueInTicksForSite("scan-arrive", targetNode.siteId, totalTicks - 20) {
            scanResultService.areaScan(run, targetNode)
        }
    }

    fun scanFromInside(run: Run, targetNode: Node) {
        stompService.toRun(run.runId, ServerActions.SERVER_HACKER_SCANS_NODE, "userId" to currentUserService.userId, "nodeId" to targetNode.id, "timings" to HACKER_SCANS_NODE_Timings)
        userTaskRunner.queueInTicksForSite("internalscan-complete", run.siteId, HACKER_SCANS_NODE_Timings.totalTicks) {
            scanResultService.areaScan(run, targetNode)
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

    fun quickScan(run: Run) {
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