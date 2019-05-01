package org.n1.mainframe.backend.service.scan

import org.n1.mainframe.backend.model.scan.NodeScan
import org.n1.mainframe.backend.model.scan.NodeStatus
import org.n1.mainframe.backend.model.scan.ProbeScanAction
import org.n1.mainframe.backend.model.scan.Scan
import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.site.SiteFull
import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.n1.mainframe.backend.service.site.*
import org.springframework.stereotype.Service
import java.security.Principal
import java.util.*

/** This service deals with the action of scanning (as opposed to the actions performed on a scan). */
@Service
class ScanningService(val scanService: ScanService,
                      val siteDataService: SiteDataService,
                      val siteService: SiteService,
                      val stompService: StompService,
                      val nodeService: NodeService,
                      val connectionService: ConnectionService,
                      val layoutService: LayoutService) {

    data class ScanResponse(val scanId: String?, val message: NotyMessage?)

    fun scanSite(siteName: String): ScanResponse {
        val siteData = siteDataService.findByName(siteName) ?: return ScanResponse(null, NotyMessage("error", "Error", "Site '${siteName}' not found"))
        val nodeScans = createNodeScans(siteData.id)
        val id = scanService.createScan(siteData, nodeScans)
        return ScanResponse(id, null)
    }

    fun createNodeScans(siteId: String): MutableMap<String, NodeScan> {
        val traverseNodes = determineDistances(siteId)
        return traverseNodes.map {
            val nodeStatus = when (it.value.distance) {
                0 -> NodeStatus.DISCOVERED
                else -> NodeStatus.UNDISCOVERED
            }
            it.key to NodeScan(status = nodeStatus, distance = it.value.distance)
        }.toMap().toMutableMap()
    }

    data class TraverseNode(val id: String,
                            var distance: Int? = null,
                            val connections: MutableSet<TraverseNode> = HashSet()) {

        override fun equals(other: Any?): Boolean {
            return if (other is TraverseNode) {
                other.id == this.id
            } else false
        }

        override fun hashCode(): Int {
            return this.id.hashCode()
        }

        override fun toString(): String {
            return "${id}(d: ${distance} connect count: ${connections.size}"
        }
    }

    fun determineDistances(siteId: String): Map<String, TraverseNode> {
        val siteData = siteDataService.getById(siteId)
        val nodes = nodeService.getAll(siteId)
        val startNodeId = siteService.findStartNode(siteData.startNodeNetworkId, nodes)?.id ?: throw IllegalStateException("Invalid start node network ID")
        val traverseNodesById = createTraverseNodes(siteId)

        val startTraverseNode = traverseNodesById[startNodeId]!!
        traverse(startTraverseNode, 0)
        return traverseNodesById
    }

    private fun createTraverseNodes(siteId: String): Map<String, TraverseNode> {
        val nodes = nodeService.getAll(siteId)
        val connections = connectionService.getAll(siteId)

        val traverseNodes = nodes.map { TraverseNode(id = it.id) }
        val traverseNodesById = traverseNodes.map { it.id to it }.toMap()
        connections.forEach {
            val from = traverseNodesById[it.fromId] ?: throw IllegalStateException("Node ${it.fromId} not found in ${siteId} in ${it.id}")
            val to = traverseNodesById[it.toId] ?: throw IllegalStateException("Node ${it.toId} not found in ${siteId} in ${it.id}")
            from.connections.add(to)
            to.connections.add(from)
        }
        return traverseNodesById
    }

    fun traverse(node: TraverseNode, distance: Int) {
        node.distance = distance
        node.connections
                .filter { it.distance == null }
                .forEach { traverse(it, distance + 1) }
    }


    // --- //

    data class ScanAndSite(val scan: Scan, val site: SiteFull)

    fun sendScanToUser(scanId: String, principal: Principal) {
        val scan = scanService.getById(scanId)
        val siteFull = siteService.getSiteFull(scan.siteId)

        val scanAndSite = ScanAndSite(scan, siteFull)
        stompService.toUser(principal, ReduxActions.SERVER_SCAN_FULL, scanAndSite)

    }

    fun processCommand(scanId: String, command: String, principal: Principal) {
        if (command != "scan") {
            stompService.terminalReceive(principal, "Unknown command, try [i]scan[/].")
            return
        }
        val scan = scanService.getById(scanId)
        val probeAction = createProbeAction(scan)
        stompService.toSite(scan.siteId, ReduxActions.SERVER_PROBE_LAUNCH, probeAction)
    }

    data class ProbeAction(val path: List<String>, val type: ProbeScanAction, val value: String)

    fun createProbeAction(scan: Scan): ProbeAction {
        val targetNode = findProbeTarget(scan)

        val path = LinkedList<String>()
        path.add(targetNode.id)
        var currentNode = targetNode
        while (currentNode.distance != 0) {
            currentNode = currentNode.connections.find { it.distance == (currentNode.distance!! - 1) }!!
            path.add(0, currentNode.id)
        }
        val action = ProbeAction(path = path, type = ProbeScanAction.SCAN_NODE_INITIAL, value = "burp")
        return action
    }

    /**
     * Of all nodes that are know to the players, find the one of which the least is known (lowest scan status level).
     */
    private fun findProbeTarget(scan: Scan): TraverseNode {
        val traverseNodeValues = createTraverseNodes(scan.siteId).values
        traverseNodeValues.forEach { it.distance = scan.nodeScanById[it.id]!!.distance }
        val traverseNodes = traverseNodeValues.filter { scan.nodeScanById[it.id]!!.status != NodeStatus.UNDISCOVERED }
        val distanceSortedNodes = traverseNodes.sortedBy { it.distance }
        val targetNode = distanceSortedNodes.minBy {
            scan.nodeScanById[it.id]!!.status.level
        }!!
        return targetNode
    }

    //---//

    fun probeArrive(scanId: String, nodeId: String, type: String, principal: Principal) {
        val scan = scanService.getById(scanId)
        val nodeScan = scan.nodeScanById[nodeId] ?: throw IllegalStateException("Node to scan ${nodeId} not part of ${scan.siteId}")
        val node = nodeService.getById(nodeId)
        if (nodeScan.status != NodeStatus.DISCOVERED) {
            stompService.terminalReceive(principal, "Scanning node ${node.networkId} did not find anything new.")
            return
        }
        nodeScan.status = NodeStatus.TYPE
        scanService.save(scan)
        val message = if (node.ice) "ice detected." else "no ice detected."
        stompService.terminalReceive(principal, "Scanning node ${node.networkId} - ${message}")

        data class ProbeResult(val nodeId: String, val newStatus: NodeStatus)
        stompService.toScan(scanId, ReduxActions.SERVER_PROBE_SCAN_SUCCESS, ProbeResult(nodeId, nodeScan.status))
    }
}