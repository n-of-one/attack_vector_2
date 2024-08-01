package org.n1.av2.run.scanning

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.connection.ServerActions.SERVER_PROBE_LAUNCH
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.run.entity.NodeScan
import org.n1.av2.run.entity.NodeScanStatus
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.runlink.RunLinkService
import org.n1.av2.run.scanning.NodeScanType.SCAN_CONNECTIONS
import org.n1.av2.run.terminal.inside.HACKER_SCANS_NODE_Timings
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class InitiateScanService(
    private val runEntityService: RunEntityService,
    private val connectionService: ConnectionService,
    private val nodeEntityService: NodeEntityService,
    private val currentUserService: CurrentUserService,
    private val traverseNodeService: TraverseNodeService,
    private val userTaskRunner: UserTaskRunner,
    private val runLinkService: RunLinkService,
    private val scanService: ScanService,
) {

    fun scanFromOutside(run: Run, startNode: Node) {
        val nodes = nodeEntityService.getAll(run.siteId)
        val (start, traverseNodesById) = traverseNodeService.createTraverseNodesWithDistance(run.siteId, startNode.id, nodes, startNode.id)
        val target: TraverseNode = traverseNodesById[startNode.id]!!

        try {
            val path = TraverseNode.createPath(start, target)

            val timings = SCAN_CONNECTIONS.timings
            connectionService.toRun(
                run.runId, SERVER_PROBE_LAUNCH,
                "probeUserId" to currentUserService.userId,
                "path" to path,
                "scanType" to SCAN_CONNECTIONS,
                "timings" to timings
            )

            val totalTicks = (path.size) * timings.connection + timings.totalWithoutConnection
            userTaskRunner.queueInTicksForSite("scan-arrive", startNode.siteId, totalTicks - 20) {
                scanService.areaScan(run, startNode, nodes)
            }

        }
        catch (exception: BlockedPathException) {
            connectionService.replyTerminalReceiveAndLocked(false, exception.message!!)
            return
        }
    }

    fun scanFromInside(run: Run, targetNode: Node) {
        val nodes = nodeEntityService.getAll(run.siteId)

        connectionService.toRun(
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
            node.id to if (node.unhackedIce) NodeScanStatus.ICE_PROTECTED_3 else NodeScanStatus.FULLY_SCANNED_4
        }.toMap()

        connectionService.toRun(run.runId, ServerActions.SERVER_DISCOVER_NODES, "nodeStatusById" to nodeStatusById)
        runLinkService.sendUpdatedRunInfoToHackers(run)
    }
}
