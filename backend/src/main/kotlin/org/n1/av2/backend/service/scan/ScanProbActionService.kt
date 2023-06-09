package org.n1.av2.backend.service.scan

import org.n1.av2.backend.entity.run.NodeScan
import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.ConnectionEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.model.ui.NodeScanType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.util.s
import org.springframework.stereotype.Service
import java.util.*

data class ProbeResultConnections(val nodeStatusById: Map<String, NodeScanStatus>, val connectionIds: Collection<String>)

@Service
class ScanProbActionService(
    private val runEntityService: RunEntityService,
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val scanInfoService: ScanInfoService,
) {


    fun probeCompleted(run: Run, node: Node, action: NodeScanType) {
        val updateScanInfo = processProbeAction(run, node, action)
        if (updateScanInfo) {
            scanInfoService.updateScanInfoToPlayers(run)
        }
    }

    fun processProbeAction(run: Run, node: Node, action: NodeScanType): Boolean {
        val nodeScan = run.nodeScanById[node.id] ?: throw IllegalStateException("Node to scan ${node.id} not part of ${run.siteId}")

        return when (action) {
            NodeScanType.SCAN_NODE_INITIAL -> probeScanInitial(run, node, nodeScan)
            NodeScanType.SCAN_CONNECTIONS -> scannedConnections(run, node, nodeScan)
            NodeScanType.SCAN_NODE_DEEP -> probeScanDeep(run, node, nodeScan)
        }
    }

    data class ProbeResultSingleNode(val nodeId: String, val newStatus: NodeScanStatus)

    private fun probeScanInitial(run: Run, node: Node, nodeScan: NodeScan): Boolean {
        stompService.replyTerminalReceive("Scanned node ${node.networkId} - discovered ${node.layers.size} ${"layer".s(node.layers.size)}.")

        scannedSingleNode(nodeScan, run, node.id, NodeScanStatus.TYPE_KNOWN_2)
        return false
    }


    private fun probeScanDeep(run: Run, node: Node, nodeScan: NodeScan): Boolean {
        if (nodeScan.status != NodeScanStatus.CONNECTIONS_KNOWN_3) {
            stompService.replyTerminalReceive("Scanning node ${node.networkId} did not find anything.")
            return false
        }

        stompService.replyTerminalReceive("Scanned node ${node.networkId} - discovered ${node.layers.size} layer details")

        scannedSingleNode(nodeScan, run, node.id, NodeScanStatus.FULLY_SCANNED_4)
        return false
    }

    fun scannedConnections(run: Run, node: Node, nodeScan: NodeScan): Boolean {

        if (nodeScan.status.level >= NodeScanStatus.CONNECTIONS_KNOWN_3.level ) {
            stompService.replyTerminalReceive("Scanning node ${node.networkId} did not find new connections.")
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


        val nodeStatusById = discoveredNodeIds.map { it to NodeScanStatus.DISCOVERED_1 }.toMap()

        stompService.toRun(run.runId, ServerActions.SERVER_UPDATE_NODE_STATUS, ProbeResultSingleNode(node.id, nodeScan.status))
        stompService.toRun(run.runId, ServerActions.SERVER_DISCOVER_NODES, ProbeResultConnections(nodeStatusById, discoveredConnectionIds))

        val iceMessage = if (node.ice) " | Ice detected" else ""
        stompService.replyTerminalReceive("Scanned node ${node.networkId} - discovered ${discoveredNodeIds.size} ${"neighbour".s(discoveredNodeIds.size)}${iceMessage}")
        return discoveredNodeIds.isNotEmpty()
    }

    fun scannedSingleNode(nodeScan: NodeScan, run: Run, nodeId: String, newStatus: NodeScanStatus) {
        nodeScan.status = newStatus
        run.totalDistanceScanned += nodeScan.distance!!
        runEntityService.save(run)
        stompService.toRun(run.runId, ServerActions.SERVER_UPDATE_NODE_STATUS, ProbeResultSingleNode(nodeId, nodeScan.status))
    }
}