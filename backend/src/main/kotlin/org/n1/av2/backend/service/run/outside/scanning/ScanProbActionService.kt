package org.n1.av2.backend.service.run.outside.scanning

import org.n1.av2.backend.engine.ScheduledTask
import org.n1.av2.backend.entity.run.NodeScan
import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.NodeScanStatus.*
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.ConnectionEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.ServerActions.SERVER_UPDATE_NODE_STATUS
import org.n1.av2.backend.service.site.ScanInfoService
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.util.s
import org.springframework.stereotype.Service
import java.util.*

@Service
class ScanProbActionService(
    private val runEntityService: RunEntityService,
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val scanInfoService: ScanInfoService,
) {

    @ScheduledTask
    fun probeCompleted(run: Run, node: Node) {

        val discoveries = traverseScanNodes(node.id, run).sortedBy { it.distance }

        var newNodesDiscoveredCount = 0

        discoveries.forEach { discovery: Discovery ->
            val oldStatus = run.nodeScanById[discovery.nodeId]!!
            val newStatus = discovery.type.nodeScanStatus
            run.nodeScanById[discovery.nodeId] = NodeScan(newStatus, oldStatus.distance)
            if (oldStatus.status == UNDISCOVERED_0) {
                newNodesDiscoveredCount++
            }
        }
        runEntityService.save(run)

        val nodeStatusById = discoveries.map { it.nodeId to it.type.nodeScanStatus }.toMap()

        stompService.toRun(run.runId, ServerActions.SERVER_DISCOVER_NODES, "nodeStatusById" to nodeStatusById)
        scanInfoService.updateScanInfoToPlayers(run)

        stompService.replyTerminalReceive("New nodes discovered: ${newNodesDiscoveredCount}")
        if (node.ice && !node.hacked) {
            stompService.replyTerminalReceive("Scan blocked by ICE at [ok]${node.networkId}")
        }
    }

    enum class DiscoveryType(val nodeScanStatus: NodeScanStatus) {
        FULL(FULLY_SCANNED_4),
        ICE(ICE_PROTECTED_3),
        EXISTENCE(UNCONNECTABLE_1)
    }

    class Discovery(val nodeId: String, val type: DiscoveryType, val distance: Int)

    fun traverseScanNodes(nodeId: String, run: Run): List<Discovery> {
        val node = nodeEntityService.findById(nodeId)

        val currentDistance = run.nodeScanById[nodeId]!!.distance

        val connectedNodeIds = run.nodeScanById
            .filter { (_, nodeScan) -> nodeScan.distance == currentDistance + 1 }
            .map { it.key }

        if (node.ice && !node.hacked) {
            return connectedNodeIds
                .map { afterIceNodeId -> Discovery(afterIceNodeId, DiscoveryType.EXISTENCE, currentDistance + 1) }
                .plus(Discovery(nodeId, DiscoveryType.ICE, currentDistance))
        }

        return connectedNodeIds
            .flatMap { neighbourNodeId -> traverseScanNodes(neighbourNodeId, run) }
            .distinct()
            .plus(Discovery(nodeId, DiscoveryType.FULL, currentDistance))
    }

    fun scannedNodeAndConnections(run: Run, node: Node, nodeScan: NodeScan): Boolean {

        val iceLeft = node.layers
            .filterIsInstance<IceLayer>()
            .any { !it.hacked }
        val newStatus = if (iceLeft) ICE_PROTECTED_3 else FULLY_SCANNED_4
        run.updateScanStatus(node.id, newStatus)

        val discoveredNodeStatus = if (iceLeft) UNCONNECTABLE_1 else CONNECTABLE_2

        val discoveredNodeIds = LinkedList<String>()
        val connections = connectionEntityService.findByNodeId(node.id)
        connections.forEach { connection ->
            val connectedNodeId = if (connection.fromId == node.id) connection.toId else connection.fromId
            val connectedNodeScanStatus = run.nodeScanById[connectedNodeId]!!.status
            if ((connectedNodeScanStatus == UNDISCOVERED_0) || (connectedNodeScanStatus == UNCONNECTABLE_1 && discoveredNodeStatus == CONNECTABLE_2)) {
                discoveredNodeIds.add(connectedNodeId)
                run.updateScanStatus(connectedNodeId, discoveredNodeStatus)
            }
        }

        val nodeStatusById = discoveredNodeIds.map { it to discoveredNodeStatus }.toMap()
        stompService.toRun(run.runId, ServerActions.SERVER_DISCOVER_NODES, "nodeStatusById" to nodeStatusById)

        stompService.toRun(run.runId, SERVER_UPDATE_NODE_STATUS, "nodeId" to node.id, "newStatus" to newStatus)

        runEntityService.save(run)

        stompService.replyTerminalReceive("Scanned node ${node.networkId} - discovered ${discoveredNodeIds.size} ${"neighbour".s(discoveredNodeIds.size)}")
        return discoveredNodeIds.isNotEmpty()
    }


//
//
//
//
//    fun processProbeAction(run: Run, node: Node, action: NodeScanType): Boolean {
//        val nodeScan = run.nodeScanById[node.id] ?: throw IllegalStateException("Node to scan ${node.id} not part of ${run.siteId}")
//
//        return when (action) {
//            NodeScanType.SCAN_NODE_INITIAL -> probeScanInitial(run, node, nodeScan)
//            NodeScanType.SCAN_CONNECTIONS -> scannedConnections(run, node, nodeScan)
//            NodeScanType.SCAN_NODE_DEEP -> probeScanDeep(run, node, nodeScan)
//        }
//    }
//
//
//    data class ProbeResultSingleNode(val nodeId: String, val newStatus: NodeScanStatus)
//
//    private fun probeScanInitial(run: Run, node: Node, nodeScan: NodeScan): Boolean {
//        stompService.replyTerminalReceive("Scanned node ${node.networkId} - discovered ${node.layers.size} ${"layer".s(node.layers.size)}.")
//
//        scannedSingleNode(nodeScan, run, node.id, NodeScanStatus.TYPE_KNOWN_2)
//        return false
//    }
//
//
//    private fun probeScanDeep(run: Run, node: Node, nodeScan: NodeScan): Boolean {
//        if (nodeScan.status != NodeScanStatus.CONNECTIONS_KNOWN_3) {
//            stompService.replyTerminalReceive("Scanning node ${node.networkId} did not find anything.")
//            return false
//        }
//
//        stompService.replyTerminalReceive("Scanned node ${node.networkId} - discovered ${node.layers.size} layer details")
//
//        scannedSingleNode(nodeScan, run, node.id, NodeScanStatus.FULLY_SCANNED_3)
//        return false
//    }
//
//    fun scannedConnections(run: Run, node: Node, nodeScan: NodeScan): Boolean {
//
//        if (nodeScan.status.level >= NodeScanStatus.CONNECTIONS_KNOWN_3.level ) {
//            stompService.replyTerminalReceive("Scanning node ${node.networkId} did not find new connections.")
//            return false
//        }
//
//        nodeScan.status = if (nodeScan.status == NodeScanStatus.TYPE_KNOWN_2) NodeScanStatus.CONNECTIONS_KNOWN_3 else NodeScanStatus.FULLY_SCANNED_3
//
//        val connections = connectionEntityService.findByNodeId(node.id)
//
//        val discoveredNodeIds = LinkedList<String>()
//        val discoveredConnectionIds = HashSet<String>()
//        connections.forEach { connection ->
//            val connectedNodeId = if (connection.fromId == node.id) connection.toId else connection.fromId
//            val connectedNode = nodeEntityService.getById(connectedNodeId)
//            if (run.nodeScanById[connectedNode.id]!!.status == NodeScanStatus.UNDISCOVERED_0) {
//                discoveredNodeIds.add(connectedNode.id)
//                discoveredConnectionIds.add(connection.id)
//                run.nodeScanById[connectedNode.id]!!.status = NodeScanStatus.DISCOVERED_1
//            }
//        }
//
//        val allDiscoveredNodes = run.nodeScanById
//            .filter { (_, nodeScan) -> nodeScan.status != NodeScanStatus.UNDISCOVERED_0 }
//            .keys
//
//        val connectionsFromDiscoveredNodes = discoveredNodeIds.flatMap { connectionEntityService.findByNodeId(it) }
//
//        val extraDiscoveredConnections = connectionsFromDiscoveredNodes
//            .filter { allDiscoveredNodes.contains(it.fromId) && allDiscoveredNodes.contains(it.toId) }
//            .map { it.id }
//
//        discoveredConnectionIds.addAll(extraDiscoveredConnections)
//
//
//        runEntityService.save(run)
//
//
//        val nodeStatusById = discoveredNodeIds.map { it to NodeScanStatus.DISCOVERED_1 }.toMap()
//
//        stompService.toRun(run.runId, ServerActions.SERVER_UPDATE_NODE_STATUS, ProbeResultSingleNode(node.id, nodeScan.status))
//        stompService.toRun(run.runId, ServerActions.SERVER_DISCOVER_NODES, ProbeResultConnections(nodeStatusById, discoveredConnectionIds))
//
//        val iceMessage = if (node.ice) " | Ice detected" else ""
//        stompService.replyTerminalReceive("Scanned node ${node.networkId} - discovered ${discoveredNodeIds.size} ${"neighbour".s(discoveredNodeIds.size)}${iceMessage}")
//        return discoveredNodeIds.isNotEmpty()
//    }
//
//    fun scannedSingleNode(nodeScan: NodeScan, run: Run, nodeId: String, newStatus: NodeScanStatus) {
//        nodeScan.status = newStatus
//        runEntityService.save(run)
//        stompService.toRun(run.runId, ServerActions.SERVER_UPDATE_NODE_STATUS, ProbeResultSingleNode(nodeId, nodeScan.status))
//    }
}