package org.n1.av2.backend.service.scan

import org.n1.av2.backend.model.db.run.NodeScan
import org.n1.av2.backend.model.db.run.NodeStatus
import org.n1.av2.backend.model.db.run.Scan
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.NodeScanType
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.ReduxActions
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


    data class ProbeAction(val probeUserId: String, val path: List<String>, val scanType: NodeScanType, val autoScan: Boolean)

    fun createProbeAction(scan: Scan, autoScan: Boolean): ProbeAction? {
        val targetNode = findProbeTarget(scan)
        val status = scan.nodeScanById[targetNode.id]!!.status
        val scanType = determineNodeScanType(status) ?: return null
        val path = createNodePath(targetNode)
        val userId = currentUserService.userId
        return ProbeAction(probeUserId = userId, path = path, scanType = scanType, autoScan = autoScan)
    }

    fun determineNodeScanType(status: NodeStatus): NodeScanType? {
        return when (status) {
            NodeStatus.DISCOVERED -> NodeScanType.SCAN_NODE_INITIAL
            NodeStatus.TYPE -> NodeScanType.SCAN_CONNECTIONS
            NodeStatus.CONNECTIONS -> NodeScanType.SCAN_NODE_DEEP
            NodeStatus.SERVICES -> null
            NodeStatus.UNDISCOVERED -> error("Cannot scan a node that has not yet been discovered.")
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
        val traverseNodes = traverseNodeValues.filter { scan.nodeScanById[it.id]!!.status != NodeStatus.UNDISCOVERED }
        val distanceSortedNodes = traverseNodes.sortedBy { it.distance }
        return distanceSortedNodes.minBy {
            scan.nodeScanById[it.id]!!.status.level
        }!!
    }

    //---//

    fun probeArrive(runId: String, nodeId: String, action: NodeScanType): Boolean {
        val scan = scanService.getById(runId)
        val node = nodeService.getById(nodeId)
        return probeArrive(scan, node, action)
    }

    fun probeArrive(scan: Scan, node: Node, action: NodeScanType): Boolean {
        val nodeScan = scan.nodeScanById[node.id] ?: throw IllegalStateException("Node to scan ${node.id} not part of ${scan.siteId}")

        return when (action) {
            NodeScanType.SCAN_NODE_INITIAL -> probeScanInitial(scan, node, nodeScan)
            NodeScanType.SCAN_CONNECTIONS -> probeScanConnection(scan, node, nodeScan)
            NodeScanType.SCAN_NODE_DEEP -> probeScanDeep(scan, node, nodeScan)
        }
    }

    data class ProbeResultInitial(val nodeId: String, val newStatus: NodeStatus)

    fun probeScanInitial(scan: Scan, node: Node, nodeScan: NodeScan): Boolean {
        stompService.terminalReceive("Scanned node ${node.networkId} - discovered ${node.services.size} ${"service".s(node.services.size)}.")

        nodeScan.status = NodeStatus.TYPE
        scan.totalDistanceScanned += nodeScan.distance!!
        scanService.save(scan)
        stompService.toRun(scan.id, ReduxActions.SERVER_UPDATE_NODE_STATUS, ProbeResultInitial(node.id, nodeScan.status))

        return false
    }

    private fun probeScanConnection(scan: Scan, node: Node, nodeScan: NodeScan): Boolean {
        if (nodeScan.status != NodeStatus.TYPE) {
            stompService.terminalReceive("Scanning node ${node.networkId} did not find new connections.")
            return false
        }
        nodeScan.status = NodeStatus.CONNECTIONS

        val connections = connectionService.findByNodeId(node.id)

        val discoveredNodeIds = LinkedList<String>()
        val discoveredConnectionIds = HashSet<String>()
        connections.forEach { connection ->
            val connectedNodeId = if (connection.fromId == node.id) connection.toId else connection.fromId
            val connectedNode = nodeService.getById(connectedNodeId)
            if (scan.nodeScanById[connectedNode.id]!!.status == NodeStatus.UNDISCOVERED) {
                discoveredNodeIds.add(connectedNode.id)
                discoveredConnectionIds.add(connection.id)
                scan.nodeScanById[connectedNode.id]!!.status = NodeStatus.DISCOVERED
            }
        }

        val allDiscoveredNodes = scan.nodeScanById
                .filter { (_, nodeScan) -> nodeScan.status != NodeStatus.UNDISCOVERED }
                .keys

        val connectionsFromDiscoveredNodes = discoveredNodeIds.flatMap { connectionService.findByNodeId(it) }

        val extraDiscoveredConnections = connectionsFromDiscoveredNodes
                .filter { allDiscoveredNodes.contains(it.fromId) && allDiscoveredNodes.contains(it.toId) }
                .map { it.id }

        discoveredConnectionIds.addAll(extraDiscoveredConnections)


        scan.totalDistanceScanned += nodeScan.distance!!
        scanService.save(scan)
        stompService.toRun(scan.id, ReduxActions.SERVER_UPDATE_NODE_STATUS, ProbeResultInitial(node.id, nodeScan.status))

        data class ProbeResultConnections(val nodeIds: List<String>, val connectionIds: Collection<String>)
        stompService.toRun(scan.id, ReduxActions.SERVER_DISCOVER_NODES, ProbeResultConnections(discoveredNodeIds, discoveredConnectionIds))

        val iceMessage = if (node.ice) " | Ice detected" else ""
        stompService.terminalReceive("Scanned node ${node.networkId} - discovered ${discoveredNodeIds.size} ${"neighbour".s(discoveredNodeIds.size)}${iceMessage}")
        return discoveredNodeIds.isNotEmpty()
    }

    private fun probeScanDeep(scan: Scan, node: Node, nodeScan: NodeScan): Boolean {
        if (nodeScan.status != NodeStatus.CONNECTIONS) {
            stompService.terminalReceive("Scanning node ${node.networkId} did not find anything.")
            return false
        }

        stompService.terminalReceive("Scanned node ${node.networkId} - discovered ${node.services.size} service details")

        nodeScan.status = NodeStatus.SERVICES
        scan.totalDistanceScanned += nodeScan.distance!!
        scanService.save(scan)
        stompService.toRun(scan.id, ReduxActions.SERVER_UPDATE_NODE_STATUS, ProbeResultInitial(node.id, nodeScan.status))
        return false
    }


    fun quickScanNode(node: Node, scan: Scan) {
        val status = scan.nodeScanById[node.id]!!.status
        if (status == NodeStatus.SERVICES) return
        if (status == NodeStatus.UNDISCOVERED || status == NodeStatus.DISCOVERED) {
            probeArrive(scan, node, NodeScanType.SCAN_NODE_INITIAL)
        }

        val newStatus = scan.nodeScanById[node.id]!!.status
        if (status == NodeStatus.TYPE || newStatus == NodeStatus.TYPE) {
            probeArrive(scan, node, NodeScanType.SCAN_CONNECTIONS)
        }
        probeArrive(scan, node, NodeScanType.SCAN_NODE_DEEP)
    }
}