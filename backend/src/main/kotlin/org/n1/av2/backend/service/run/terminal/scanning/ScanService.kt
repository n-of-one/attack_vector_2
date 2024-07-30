package org.n1.av2.backend.service.run.terminal.scanning

import org.n1.av2.backend.engine.ScheduledTask
import org.n1.av2.backend.entity.run.NodeScan
import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.NodeScanStatus.*
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.site.RunLinkService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class ScanService(
    private val runEntityService: RunEntityService,
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val runLinkService: RunLinkService,
    private val traverseNodeService: TraverseNodeService,
    private val siteService: SiteService,
) {

    @ScheduledTask
    fun hackerArrivedNodeScan(nodeId: String, userId: String, runId: String) {
        val run = runEntityService.getByRunId(runId)
        val nodeScan = run.nodeScanById[nodeId]!!

        if (nodeScan.status != CONNECTABLE_2) {
            return
        }  // another @ScheduledTask has changed the state of things, nothing left to do.

        val nodes = nodeEntityService.getAll(run.siteId)
        val targetNode = nodes.find { it.id == nodeId }!!
        areaScan(run, targetNode, nodes)
    }

    @ScheduledTask
    fun areaScan(run: Run, node: Node, nodes: List<Node>) {
        if (node.unhackedIce) {
            stompService.replyTerminalReceive("Scan blocked by ICE at [ok]${node.networkId}")
        }

        val traverseNodesById = traverseNodeService.createTraverseNodes(run.siteId, nodes)
        val discoveries = determineDiscoveries(node.id, run, traverseNodesById)

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

        val nodeStatusById = discoveries.map { it.nodeId to it.scanStatus }.toMap()

        stompService.toRun(run.runId, ServerActions.SERVER_DISCOVER_NODES, "nodeStatusById" to nodeStatusById)
        runLinkService.sendUpdatedRunInfoToHackers(run)

        stompService.replyTerminalReceive("New nodes discovered: ${newNodesDiscoveredCount}")
    }

    class Discovery(val nodeId: String, val scanStatus: NodeScanStatus)


    private fun determineDiscoveries(nodeId: String, run: Run, traverseNodesById: Map<String, TraverseNode>): List<Discovery> {

        val target = traverseNodesById[nodeId]!!
        val scannedNodes: List<TraverseNode> = target.unblockedNetwork(emptyList())

        val scannedIceNodes = scannedNodes.filter { it.unhackedIce }
        val discoveredIceNodes = scannedIceNodes.filter { run.nodeScanById[it.nodeId]!!.status.rank < ICE_PROTECTED_3.rank }
        val iceDiscoveries = discoveredIceNodes.map { Discovery(it.nodeId, ICE_PROTECTED_3) }

        val scannedRegularNodes = scannedNodes - scannedIceNodes.toSet()
        val discoveredRegularNodes = scannedRegularNodes.filter { run.nodeScanById[it.nodeId]!!.status.rank < FULLY_SCANNED_4.rank }
        val regularDiscoveris = discoveredRegularNodes.map { Discovery(it.nodeId, FULLY_SCANNED_4) }

        val iceNeighbors = discoveredIceNodes.flatMap { it.connections }.distinct()
        val discoveredIceNeighbors = (iceNeighbors - scannedNodes.toSet()).filter { run.nodeScanById[it.nodeId]!!.status.rank < UNCONNECTABLE_1.rank }
        val neighborDiscoveries = discoveredIceNeighbors.map { Discovery(it.nodeId, UNCONNECTABLE_1) }

        return (iceDiscoveries + regularDiscoveris + neighborDiscoveries)
    }

    fun createInitialNodeScans(siteId: String): MutableMap<String, NodeScan> {
        val nodes = nodeEntityService.getAll(siteId)
        val startNode = siteService.findStartNode(nodes) ?: throw IllegalStateException("Start node of site does not exist")
        val traverseNodes = traverseNodeService.createTraverseNodesWithDistance(siteId, startNode.id, nodes)

        return traverseNodes.map {
            val nodeStatus = when (it.value.distance) {
                1 -> NodeScanStatus.CONNECTABLE_2
                else -> NodeScanStatus.UNDISCOVERED_0
            }
            it.key to NodeScan(status = nodeStatus, distance = it.value.distance!!)
        }.toMap().toMutableMap()
    }

}
