package org.n1.av2.run.scanning

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.ScheduledTask
import org.n1.av2.run.entity.NodeScan
import org.n1.av2.run.entity.NodeScanStatus
import org.n1.av2.run.entity.NodeScanStatus.*
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.runlink.RunLinkService
import org.n1.av2.site.SiteService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service

@Service
class ScanService(
    private val runEntityService: RunEntityService,
    private val connectionService: ConnectionService,
    private val nodeEntityService: NodeEntityService,
    private val runLinkService: RunLinkService,
    private val traverseNodeService: TraverseNodeService,
    private val siteService: SiteService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
) {

    @ScheduledTask
    fun hackerArrivedNodeScan(nodeId: String, runId: String) {
        val run = runEntityService.getByRunId(runId)
        val nodeScan = run.nodeScanById[nodeId]!!

        if (nodeScan.status != CONNECTABLE_2) {
            return
        }  // another @ScheduledTask has changed the state of things, nothing left to do.

        val nodes = nodeEntityService.findBySiteId(run.siteId)
        val targetNode = nodes.find { it.id == nodeId }!!
        areaScan(run, targetNode, nodes, false)
    }

    @ScheduledTask
    fun areaScan(run: Run, scanNode: Node, nodes: List<Node>, ignoreIceAtScanNode: Boolean) {
        if (sitePropertiesEntityService.getBySiteId(run.siteId).shutdownEnd != null ) {
            // the site has shut down since we started scanning
            connectionService.replyTerminalSetLocked(false)
            return
        }
        if (scanNode.unhackedIce && !ignoreIceAtScanNode) {
            connectionService.replyTerminalReceive("Scan blocked by ICE at [ok]${scanNode.networkId}")
        }

        val traverseNodesById = traverseNodeService.createTraverseNodes(run.siteId, nodes)

        val discoveries = determineDiscoveries(scanNode.id, run, traverseNodesById, ignoreIceAtScanNode)

        var newNodesDiscoveredCount = 0
        discoveries.forEach { discovery: Discovery ->
            val oldStatus = run.nodeScanById[discovery.nodeId]!!
            val newStatus = discovery.scanStatus
            run.nodeScanById[discovery.nodeId] = NodeScan(newStatus, oldStatus.distance)
            if (oldStatus.status == UNDISCOVERED_0) {
                newNodesDiscoveredCount++
            }
        }
        runEntityService.save(run)

        val nodeStatusById = discoveries.associate { it.nodeId to it.scanStatus }

        connectionService.toRun(run.runId, ServerActions.SERVER_DISCOVER_NODES, "nodeStatusById" to nodeStatusById)
        runLinkService.sendUpdatedRunInfoToHackers(run)

        connectionService.replyTerminalReceive("New nodes discovered: ${newNodesDiscoveredCount}")
    }

    class Discovery(val nodeId: String, val scanStatus: NodeScanStatus)


    private fun determineDiscoveries(targetNodeId: String, run: Run, traverseNodesById: Map<String, TraverseNode>, ignoreIceAtScanNode: Boolean): List<Discovery> {

        val target = traverseNodesById[targetNodeId]!!
        val scannedNodes: List<TraverseNode> = target.unblockedNetwork(emptyList(), ignoreIceAtScanNode)

        val scannedIceNodes = scannedNodes.filter { it.unhackedIce }
        val discoveredIceNodes = scannedIceNodes.filter { run.nodeScanById[it.nodeId]!!.status.rank < ICE_PROTECTED_3.rank }
        val iceDiscoveries = discoveredIceNodes.map { Discovery(it.nodeId, ICE_PROTECTED_3) }

        val scannedRegularNodes = scannedNodes - scannedIceNodes.toSet()
        val discoveredRegularNodes = scannedRegularNodes.filter { run.nodeScanById[it.nodeId]!!.status.rank < FULLY_SCANNED_4.rank }
        val regularDiscoveries = discoveredRegularNodes.map { Discovery(it.nodeId, FULLY_SCANNED_4) }

        val iceNeighbors = discoveredIceNodes.flatMap { it.connections }.distinct()
        val discoveredIceNeighbors = (iceNeighbors - scannedNodes.toSet()).filter { run.nodeScanById[it.nodeId]!!.status.rank < UNCONNECTABLE_1.rank }
        val neighborDiscoveries = discoveredIceNeighbors.map { Discovery(it.nodeId, UNCONNECTABLE_1) }

        return (iceDiscoveries + regularDiscoveries + neighborDiscoveries)
    }

    fun createInitialNodeScans(siteId: String): MutableMap<String, NodeScan> {
        val nodes = nodeEntityService.findBySiteId(siteId)
        val startNode = siteService.findStartNode(nodes) ?: throw IllegalStateException("Start node of site does not exist")
        val traverseNodes = traverseNodeService.createTraverseNodesWithDistance(siteId, startNode.id, nodes)

        return traverseNodes.map {
            val nodeStatus = when (it.value.distance) {
                1 -> CONNECTABLE_2
                else -> UNDISCOVERED_0
            }
            it.key to NodeScan(status = nodeStatus, distance = it.value.distance!!)
        }.toMap().toMutableMap()
    }

}
