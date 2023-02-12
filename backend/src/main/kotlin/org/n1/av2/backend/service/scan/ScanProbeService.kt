package org.n1.av2.backend.service.scan

import org.n1.av2.backend.model.Ticks
import org.n1.av2.backend.model.db.run.NodeScan
import org.n1.av2.backend.model.db.run.NodeScanStatus
import org.n1.av2.backend.model.db.run.Scan
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.NodeScanType
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.site.ConnectionService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.util.s
import org.springframework.stereotype.Service
import java.util.*

@Service
class ScanProbeService(
        val scanService: ScanService,
        val stompService: StompService,
        val nodeService: NodeService,
        val connectionService: ConnectionService,
        val currentUserService: CurrentUserService,
        val traverseNodeService: TraverseNodeService
) {


    data class ProbeAction(val probeUserId: String, val path: List<String>, val scanType: NodeScanType, val autoScan: Boolean, val ticks: Ticks)

    fun createProbeAction(scan: Scan, autoScan: Boolean): ProbeAction? {
        val targetNode = findProbeTarget(scan)
        val status = scan.nodeScanById[targetNode.id]!!.status
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
    private fun findProbeTarget(scan: Scan): TraverseNode {
        val traverseNodeValues = traverseNodeService.createTraverseNodesWithDistance(scan).values
        val traverseNodes = traverseNodeValues.filter { scan.nodeScanById[it.id]!!.status != NodeScanStatus.UNDISCOVERED_0 }
        val distanceSortedNodes = traverseNodes.sortedBy { it.distance }
        return distanceSortedNodes.minBy {
            scan.nodeScanById[it.id]!!.status.level
        }
    }

    //---//

    fun probeArrive(runId: String, nodeId: String, action: NodeScanType, autoScan: Boolean): Boolean {
        val scan = scanService.getByRunId(runId)
        val node = nodeService.getById(nodeId)
        return probeArrive(scan, node, action, autoScan)
    }

    fun probeArrive(scan: Scan, node: Node, action: NodeScanType, autoScan: Boolean): Boolean {
        val nodeScan = scan.nodeScanById[node.id] ?: throw IllegalStateException("Node to scan ${node.id} not part of ${scan.siteId}")

        return when (action) {
            NodeScanType.SCAN_NODE_INITIAL -> probeScanInitial(scan, node, nodeScan, autoScan)
            NodeScanType.SCAN_CONNECTIONS -> scannedConnections(scan, node, nodeScan, autoScan)
            NodeScanType.SCAN_NODE_DEEP -> probeScanDeep(scan, node, nodeScan, autoScan)
        }
    }

    data class ProbeResultSingleNode(val nodeId: String, val newStatus: NodeScanStatus)

    private fun probeScanInitial(scan: Scan, node: Node, nodeScan: NodeScan, autoScan: Boolean): Boolean {
        val locked = autoScan
        stompService.terminalReceiveAndLockedCurrentUser(locked, "Scanned node ${node.networkId} - discovered ${node.layers.size} ${"layer".s(node.layers.size)}.")

        scannedSingleNode(nodeScan, scan, node, NodeScanStatus.TYPE_KNOWN_2)
        return false
    }

    fun scannedConnections(scan: Scan, node: Node, nodeScan: NodeScan, autoScan: Boolean): Boolean {
        val locked = autoScan

        if (nodeScan.status.level >= NodeScanStatus.CONNECTIONS_KNOWN_3.level ) {
            stompService.terminalReceiveAndLockedCurrentUser(locked,"Scanning node ${node.networkId} did not find new connections.")
            return false
        }

        nodeScan.status = if (nodeScan.status == NodeScanStatus.TYPE_KNOWN_2) NodeScanStatus.CONNECTIONS_KNOWN_3 else NodeScanStatus.FULLY_SCANNED_4

        val connections = connectionService.findByNodeId(node.id)

        val discoveredNodeIds = LinkedList<String>()
        val discoveredConnectionIds = HashSet<String>()
        connections.forEach { connection ->
            val connectedNodeId = if (connection.fromId == node.id) connection.toId else connection.fromId
            val connectedNode = nodeService.getById(connectedNodeId)
            if (scan.nodeScanById[connectedNode.id]!!.status == NodeScanStatus.UNDISCOVERED_0) {
                discoveredNodeIds.add(connectedNode.id)
                discoveredConnectionIds.add(connection.id)
                scan.nodeScanById[connectedNode.id]!!.status = NodeScanStatus.DISCOVERED_1
            }
        }

        val allDiscoveredNodes = scan.nodeScanById
                .filter { (_, nodeScan) -> nodeScan.status != NodeScanStatus.UNDISCOVERED_0 }
                .keys

        val connectionsFromDiscoveredNodes = discoveredNodeIds.flatMap { connectionService.findByNodeId(it) }

        val extraDiscoveredConnections = connectionsFromDiscoveredNodes
                .filter { allDiscoveredNodes.contains(it.fromId) && allDiscoveredNodes.contains(it.toId) }
                .map { it.id }

        discoveredConnectionIds.addAll(extraDiscoveredConnections)


        scan.totalDistanceScanned += nodeScan.distance!!
        scanService.save(scan)

        data class ProbeResultConnections(val nodeIds: List<String>, val connectionIds: Collection<String>)
        stompService.toRun(scan.runId, ReduxActions.SERVER_DISCOVER_NODES, ProbeResultConnections(discoveredNodeIds, discoveredConnectionIds))
        stompService.toRun(scan.runId, ReduxActions.SERVER_UPDATE_NODE_STATUS, ProbeResultSingleNode(node.id, nodeScan.status))

        val iceMessage = if (node.ice) " | Ice detected" else ""
        stompService.terminalReceiveAndLockedCurrentUser(locked,"Scanned node ${node.networkId} - discovered ${discoveredNodeIds.size} ${"neighbour".s(discoveredNodeIds.size)}${iceMessage}")
        return discoveredNodeIds.isNotEmpty()
    }

    private fun probeScanDeep(scan: Scan, node: Node, nodeScan: NodeScan, autoScan: Boolean): Boolean {
        if (nodeScan.status != NodeScanStatus.CONNECTIONS_KNOWN_3) {
            val locked = autoScan
            stompService.terminalReceiveAndLockedCurrentUser(locked,"Scanning node ${node.networkId} did not find anything.")
            return false
        }

        stompService.terminalReceiveAndLockedCurrentUser(false,"Scanned node ${node.networkId} - discovered ${node.layers.size} layer details")

        scannedSingleNode(nodeScan, scan, node, NodeScanStatus.FULLY_SCANNED_4)
        return false
    }

    private fun scannedSingleNode(nodeScan: NodeScan, scan: Scan, node: Node, newStatus: NodeScanStatus) {
        scannedSingleNode(nodeScan, scan, node.id, newStatus)
    }

    fun scannedSingleNode(nodeScan: NodeScan, scan: Scan, nodeId: String, newStatus: NodeScanStatus) {
        nodeScan.status = newStatus
        scan.totalDistanceScanned += nodeScan.distance!!
        scanService.save(scan)
        stompService.toRun(scan.runId, ReduxActions.SERVER_UPDATE_NODE_STATUS, ProbeResultSingleNode(nodeId, nodeScan.status))
    }


    fun quickScanNode(node: Node, scan: Scan) {
        val status = scan.nodeScanById[node.id]!!.status
        if (status == NodeScanStatus.FULLY_SCANNED_4) return
        if (status == NodeScanStatus.UNDISCOVERED_0 || status == NodeScanStatus.DISCOVERED_1) {
            probeArrive(scan, node, NodeScanType.SCAN_NODE_INITIAL, false)
        }

        val newStatus = scan.nodeScanById[node.id]!!.status
        if (status == NodeScanStatus.TYPE_KNOWN_2 || newStatus == NodeScanStatus.TYPE_KNOWN_2) {
            probeArrive(scan, node, NodeScanType.SCAN_CONNECTIONS, false)
        }
        probeArrive(scan, node, NodeScanType.SCAN_NODE_DEEP, false)
    }
}