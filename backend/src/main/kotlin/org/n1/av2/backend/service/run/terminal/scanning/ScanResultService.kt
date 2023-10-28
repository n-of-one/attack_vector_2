package org.n1.av2.backend.service.run.terminal.scanning

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

@Service
class ScanResultService(
    private val runEntityService: RunEntityService,
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val scanInfoService: ScanInfoService,
) {


    @ScheduledTask
    fun hackerArrivedNodeScan(nodeId: String, userId: String, runId: String) {
        val run = runEntityService.getByRunId(runId)
        val nodeScan = run.nodeScanById[nodeId]!!

        if (nodeScan.status != CONNECTABLE_2) { return }  // another @ScheduledTask has changed the state of things, nothing left to do.

        val node = nodeEntityService.getById(nodeId)

        val (newStatus, discoveredNeighbourStatus) = determineStatuses(node)
        run.updateScanStatus(node.id, newStatus)
        val discoveredNodeIds = discoverNodesFromHackerArriveScan(node, run, discoveredNeighbourStatus)

        val nodeStatusById = discoveredNodeIds.map { it to discoveredNeighbourStatus }.toMap()
        stompService.toRun(run.runId, ServerActions.SERVER_DISCOVER_NODES, "nodeStatusById" to nodeStatusById)
        stompService.toRun(run.runId, SERVER_UPDATE_NODE_STATUS, "nodeId" to node.id, "newStatus" to newStatus)

        runEntityService.save(run)
        stompService.replyTerminalReceive("Scanned node ${node.networkId} - discovered ${discoveredNodeIds.size} ${"neighbour".s(discoveredNodeIds.size)}")
    }

    private fun determineStatuses(node: Node): Pair<NodeScanStatus, NodeScanStatus> {
        val iceLeft = node.layers
            .filterIsInstance<IceLayer>()
            .any { !it.hacked }
        val newStatus = if (iceLeft) ICE_PROTECTED_3 else FULLY_SCANNED_4
        val discoveredNodeStatus = if (iceLeft) UNCONNECTABLE_1 else CONNECTABLE_2
        return Pair(newStatus, discoveredNodeStatus)
    }

    private fun discoverNodesFromHackerArriveScan(node: Node, run: Run, discoveredNodeStatus: NodeScanStatus): List<String> {

        val discoveredNodeIds = connectionEntityService.findByNodeId(node.id)
            .filter { connection ->
                val connectedNodeId = if (connection.fromId == node.id) connection.toId else connection.fromId
                val connectedNodeScanStatus = run.nodeScanById[connectedNodeId]!!.status
                ((connectedNodeScanStatus == UNDISCOVERED_0) || (connectedNodeScanStatus == UNCONNECTABLE_1 && discoveredNodeStatus == CONNECTABLE_2))
            }
            .map { connection ->
                if (connection.fromId == node.id) connection.toId else connection.fromId
            }

        discoveredNodeIds.forEach { discoveredNodeId -> run.updateScanStatus(discoveredNodeId, discoveredNodeStatus) }
        return discoveredNodeIds
    }


    @ScheduledTask
    fun areaScan(run: Run, node: Node) {

        val discoveries = traverseScanNodes(node.id, run).sortedBy { it.distance }

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
        scanInfoService.updateScanInfoToPlayers(run)

        stompService.replyTerminalReceive("New nodes discovered: ${newNodesDiscoveredCount}")
        if (node.ice) {
            stompService.replyTerminalReceive("Scan blocked by ICE at [ok]${node.networkId}")
        }
    }

    class Discovery(val nodeId: String, val scanStatus: NodeScanStatus, val distance: Int)

    fun traverseScanNodes(nodeId: String, run: Run): List<Discovery> {
        val node = nodeEntityService.findById(nodeId)

        val currentDistance = run.nodeScanById[nodeId]!!.distance

        val connectedNodeIds = run.nodeScanById
            .filter { (_, nodeScan) -> nodeScan.distance == currentDistance + 1 }
            .map { it.key }

        if (node.ice) {
            val iceNodeDiscoveryType = if (node.hacked) FULLY_SCANNED_4 else ICE_PROTECTED_3
            val neighbourDiscoveryType = if (node.hacked) CONNECTABLE_2 else UNCONNECTABLE_1
            return connectedNodeIds
                .map { afterIceNodeId -> Discovery(afterIceNodeId, neighbourDiscoveryType, currentDistance + 1) }
                .plus(Discovery(nodeId, iceNodeDiscoveryType, currentDistance))
        }

        return connectedNodeIds
            .flatMap { neighbourNodeId -> traverseScanNodes(neighbourNodeId, run) }
            .distinct()
            .plus(Discovery(nodeId, FULLY_SCANNED_4, currentDistance))
    }
}