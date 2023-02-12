package org.n1.av2.backend.service.scan

import org.n1.av2.backend.entity.run.NodeScan
import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.ConnectionEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.model.Ticks
import org.n1.av2.backend.model.ui.NodeScanType
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.util.s
import org.springframework.stereotype.Service
import java.util.*

@Service
class ScanProbeService(
    val runEntityService: RunEntityService,
    val stompService: StompService,
    val nodeEntityService: NodeEntityService,
    val connectionEntityService: ConnectionEntityService,
    val currentUserService: CurrentUserService,
    val traverseNodeService: TraverseNodeService
) {


    data class ProbeAction(val probeUserId: String, val path: List<String>, val scanType: NodeScanType, val autoScan: Boolean, val ticks: Ticks)

    fun createProbeAction(run: Run, autoScan: Boolean): ProbeAction? {
        val targetNode = findProbeTarget(run)
        val status = run.nodeScanById[targetNode.id]!!.status
        val scanType = determineNodeScanType(status) ?: return null
        val path = createNodePath(targetNode)
        val userId = currentUserService.userId
        return ProbeAction(probeUserId = userId, path = path, scanType = scanType, autoScan = autoScan, ticks = scanType.ticks)
    }

    fun determineNodeScanType(status: NodeScanStatus): NodeScanType? {
        return when (status) {
            NodeScanStatus.DISCOVERED_1 -> NodeScanType.SCAN_NODE_INITIAL
            NodeScanStatus.TYPE_KNOWN_2 -> NodeScanType.SCAN_CONNECTIONS
            NodeScanStatus.CONNECTIONS_KNOWN_3 -> NodeScanType.SCAN_NODE_DEEP
            NodeScanStatus.FULLY_SCANNED_4 -> null
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
    private fun findProbeTarget(run: Run): TraverseNode {
        val traverseNodeValues = traverseNodeService.createTraverseNodesWithDistance(run).values
        val traverseNodes = traverseNodeValues.filter { run.nodeScanById[it.id]!!.status != NodeScanStatus.UNDISCOVERED_0 }
        val distanceSortedNodes = traverseNodes.sortedBy { it.distance }
        return distanceSortedNodes.minBy {
            run.nodeScanById[it.id]!!.status.level
        }
    }

    //---//

    fun probeArrive(runId: String, nodeId: String, action: NodeScanType, autoScan: Boolean): Boolean {
        val scan = runEntityService.getByRunId(runId)
        val node = nodeEntityService.getById(nodeId)
        return probeArrive(scan, node, action, autoScan)
    }

    fun probeArrive(run: Run, node: Node, action: NodeScanType, autoScan: Boolean): Boolean {
        val nodeScan = run.nodeScanById[node.id] ?: throw IllegalStateException("Node to scan ${node.id} not part of ${run.siteId}")

        return when (action) {
            NodeScanType.SCAN_NODE_INITIAL -> probeScanInitial(run, node, nodeScan, autoScan)
            NodeScanType.SCAN_CONNECTIONS -> scannedConnections(run, node, nodeScan, autoScan)
            NodeScanType.SCAN_NODE_DEEP -> probeScanDeep(run, node, nodeScan, autoScan)
        }
    }

    data class ProbeResultSingleNode(val nodeId: String, val newStatus: NodeScanStatus)

    private fun probeScanInitial(run: Run, node: Node, nodeScan: NodeScan, autoScan: Boolean): Boolean {
        val locked = autoScan
        stompService.terminalReceiveAndLockedCurrentUser(locked, "Scanned node ${node.networkId} - discovered ${node.layers.size} ${"layer".s(node.layers.size)}.")

        scannedSingleNode(nodeScan, run, node, NodeScanStatus.TYPE_KNOWN_2)
        return false
    }

    fun scannedConnections(run: Run, node: Node, nodeScan: NodeScan, autoScan: Boolean): Boolean {
        val locked = autoScan

        if (nodeScan.status.level >= NodeScanStatus.CONNECTIONS_KNOWN_3.level ) {
            stompService.terminalReceiveAndLockedCurrentUser(locked,"Scanning node ${node.networkId} did not find new connections.")
            return false
        }

        nodeScan.status = if (nodeScan.status == NodeScanStatus.TYPE_KNOWN_2) NodeScanStatus.CONNECTIONS_KNOWN_3 else NodeScanStatus.FULLY_SCANNED_4

        val connections = connectionEntityService.findByNodeId(node.id)

        val discoveredNodeIds = LinkedList<String>()
        val discoveredConnectionIds = HashSet<String>()
        connections.forEach { connection ->
            val connectedNodeId = if (connection.fromId == node.id) connection.toId else connection.fromId
            val connectedNode = nodeEntityService.getById(connectedNodeId)
            if (run.nodeScanById[connectedNode.id]!!.status == NodeScanStatus.UNDISCOVERED_0) {
                discoveredNodeIds.add(connectedNode.id)
                discoveredConnectionIds.add(connection.id)
                run.nodeScanById[connectedNode.id]!!.status = NodeScanStatus.DISCOVERED_1
            }
        }

        val allDiscoveredNodes = run.nodeScanById
                .filter { (_, nodeScan) -> nodeScan.status != NodeScanStatus.UNDISCOVERED_0 }
                .keys

        val connectionsFromDiscoveredNodes = discoveredNodeIds.flatMap { connectionEntityService.findByNodeId(it) }

        val extraDiscoveredConnections = connectionsFromDiscoveredNodes
                .filter { allDiscoveredNodes.contains(it.fromId) && allDiscoveredNodes.contains(it.toId) }
                .map { it.id }

        discoveredConnectionIds.addAll(extraDiscoveredConnections)


        run.totalDistanceScanned += nodeScan.distance!!
        runEntityService.save(run)

        data class ProbeResultConnections(val nodeIds: List<String>, val connectionIds: Collection<String>)
        stompService.toRun(run.runId, ReduxActions.SERVER_UPDATE_NODE_STATUS, ProbeResultSingleNode(node.id, nodeScan.status))
        stompService.toRun(run.runId, ReduxActions.SERVER_DISCOVER_NODES, ProbeResultConnections(discoveredNodeIds, discoveredConnectionIds))

        val iceMessage = if (node.ice) " | Ice detected" else ""
        stompService.terminalReceiveAndLockedCurrentUser(locked,"Scanned node ${node.networkId} - discovered ${discoveredNodeIds.size} ${"neighbour".s(discoveredNodeIds.size)}${iceMessage}")
        return discoveredNodeIds.isNotEmpty()
    }

    private fun probeScanDeep(run: Run, node: Node, nodeScan: NodeScan, autoScan: Boolean): Boolean {
        if (nodeScan.status != NodeScanStatus.CONNECTIONS_KNOWN_3) {
            val locked = autoScan
            stompService.terminalReceiveAndLockedCurrentUser(locked,"Scanning node ${node.networkId} did not find anything.")
            return false
        }

        stompService.terminalReceiveAndLockedCurrentUser(false,"Scanned node ${node.networkId} - discovered ${node.layers.size} layer details")

        scannedSingleNode(nodeScan, run, node, NodeScanStatus.FULLY_SCANNED_4)
        return false
    }

    private fun scannedSingleNode(nodeScan: NodeScan, run: Run, node: Node, newStatus: NodeScanStatus) {
        scannedSingleNode(nodeScan, run, node.id, newStatus)
    }

    fun scannedSingleNode(nodeScan: NodeScan, run: Run, nodeId: String, newStatus: NodeScanStatus) {
        nodeScan.status = newStatus
        run.totalDistanceScanned += nodeScan.distance!!
        runEntityService.save(run)
        stompService.toRun(run.runId, ReduxActions.SERVER_UPDATE_NODE_STATUS, ProbeResultSingleNode(nodeId, nodeScan.status))
    }


    fun quickScanNode(node: Node, run: Run) {
        val status = run.nodeScanById[node.id]!!.status
        if (status == NodeScanStatus.FULLY_SCANNED_4) return
        if (status == NodeScanStatus.UNDISCOVERED_0 || status == NodeScanStatus.DISCOVERED_1) {
            probeArrive(run, node, NodeScanType.SCAN_NODE_INITIAL, false)
        }

        val newStatus = run.nodeScanById[node.id]!!.status
        if (status == NodeScanStatus.TYPE_KNOWN_2 || newStatus == NodeScanStatus.TYPE_KNOWN_2) {
            probeArrive(run, node, NodeScanType.SCAN_CONNECTIONS, false)
        }
        probeArrive(run, node, NodeScanType.SCAN_NODE_DEEP, false)
    }
}