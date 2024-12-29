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
import org.n1.av2.run.scanning.NodeScanType.OUTSIDE_SCAN
import org.n1.av2.run.timings.TimingsService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service

enum class NodeScanType() {
    SCAN_NODE_INITIAL, // to be used when implementing additional scanning by architects
    OUTSIDE_SCAN,
    SCAN_NODE_DEEP,  // to be used when implementing additional scanning by architects
}

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
    private val timingsService: TimingsService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
) {

    fun scanWithScript(run: Run, startNode: Node?, targetNode: Node) {
        val startNodeNetworkId = startNode?.networkId ?: sitePropertiesEntityService.getBySiteId(run.siteId).startNodeNetworkId
        val iceNodeIdToIgnore = targetNode.id
        val nodes = nodeEntityService.getAll(run.siteId)

        val startNode = nodes.find { it.networkId == startNodeNetworkId } ?: error("Start node not found for network ID: $startNodeNetworkId")


        val (start, traverseNodesById) = traverseNodeService.createTraverseNodesWithDistance(run.siteId, startNode.id, nodes, targetNode.id, iceNodeIdToIgnore)
        val target: TraverseNode = traverseNodesById[targetNode.id]!!

        scanFromOutSide(start, target, run, targetNode, nodes, true)
    }

    fun scanFromOutside(run: Run, startNode: Node) {
        val nodes = nodeEntityService.getAll(run.siteId)
        val (start, traverseNodesById) = traverseNodeService.createTraverseNodesWithDistance(run.siteId, startNode.id, nodes, startNode.id)
        val target: TraverseNode = traverseNodesById[startNode.id]!!

        scanFromOutSide(start, target, run, startNode, nodes, false)
    }

    fun scanFromOutSide(start: TraverseNode,
                        target: TraverseNode,
                        run: Run, targetNode:
                        Node, nodes: List<Node>,
                        ignoreIceAtTarget: Boolean) {
        try {
            val path = TraverseNode.createPath(start, target)

            val timings = timingsService.OUTSIDE_SCAN
            connectionService.toRun(
                run.runId, SERVER_PROBE_LAUNCH,
                "probeUserId" to currentUserService.userId,
                "path" to path,
                "scanType" to OUTSIDE_SCAN,
                "timings" to timings
            )

            val totalTicks = (path.size) * timings.connection + timings.totalWithoutConnection
            userTaskRunner.queueInTicksForSite("scan-arrive", run.siteId, totalTicks - 20) {
                scanService.areaScan(run, targetNode, nodes, ignoreIceAtTarget)
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
            "timings" to timingsService.INSIDE_SCAN
        )
        userTaskRunner.queueInTicksForSite("internalscan-complete", run.siteId, timingsService.INSIDE_SCAN.totalTicks) {
            scanService.areaScan(run, targetNode, nodes, false)
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
