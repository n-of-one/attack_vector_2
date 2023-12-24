package org.n1.av2.backend.service.run.terminal.scanning

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.entity.run.NodeScan
import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.model.ui.NodeScanType.SCAN_CONNECTIONS
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.ServerActions.SERVER_PROBE_LAUNCH
import org.n1.av2.backend.service.run.terminal.inside.HACKER_SCANS_NODE_Timings
import org.n1.av2.backend.service.site.RunLinkService
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class InitiateScanService(
    private val runEntityService: RunEntityService,
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val currentUserService: CurrentUserService,
    private val traverseNodeService: TraverseNodeService,
    private val userTaskRunner: UserTaskRunner,
    private val runLinkService: RunLinkService,
    private val scanService: ScanService,
) {

    fun scanFromOutside(run: Run, targetNode: Node) {
        val nodes = nodeEntityService.getAll(run.siteId)
        val (start, traverseNodesById) = traverseNodeService.createTraverseNodesWithDistance(run.siteId, nodes, targetNode.id)
        val target: TraverseNode = traverseNodesById[targetNode.id]!!

        try {
            val path = TraverseNode.createPath(start, target)

            val timings = SCAN_CONNECTIONS.timings
            stompService.toRun(
                run.runId, SERVER_PROBE_LAUNCH,
                "probeUserId" to currentUserService.userId,
                "path" to path,
                "scanType" to SCAN_CONNECTIONS,
                "timings" to timings
            )

            val totalTicks = (path.size) * timings.connection + timings.totalWithoutConnection
            userTaskRunner.queueInTicksForSite("scan-arrive", targetNode.siteId, totalTicks - 20) {
                scanService.areaScan(run, targetNode, nodes)
            }

        }
        catch (exception: BlockedPathException) {
            stompService.replyTerminalReceiveAndLocked(false, exception.message!!)
            return
        }
    }

    fun scanFromInside(run: Run, targetNode: Node) {
        val nodes = nodeEntityService.getAll(run.siteId)

        stompService.toRun(
            run.runId,
            ServerActions.SERVER_HACKER_SCANS_NODE,
            "userId" to currentUserService.userId,
            "nodeId" to targetNode.id,
            "timings" to HACKER_SCANS_NODE_Timings
        )
        userTaskRunner.queueInTicksForSite("internalscan-complete", run.siteId, HACKER_SCANS_NODE_Timings.totalTicks) {
            scanService.areaScan(run, targetNode, nodes)
        }
    }

    fun quickScan(run: Run) {
        val nodes = nodeEntityService.getAll(run.siteId)

        nodes.forEach {node ->
            val oldStatus = run.nodeScanById[node.id]
            run.nodeScanById[node.id] = NodeScan(NodeScanStatus.FULLY_SCANNED_4, oldStatus!!.distance)
        }
        runEntityService.save(run)

        val nodeStatusById = nodes.map { node ->
            node.id to NodeScanStatus.FULLY_SCANNED_4
        }.toMap()

        stompService.toRun(run.runId, ServerActions.SERVER_DISCOVER_NODES, "nodeStatusById" to nodeStatusById)
        runLinkService.sendUpdatedRunInfoToHackers(run)
    }
}
