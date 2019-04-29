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
                else -> NodeStatus.CONNECTIONS
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
            data class TerminalLine(val line: String)
            stompService.toUser(principal, ReduxActions.SERVER_TERMINAL_RECEIVE, TerminalLine("Unknown command, try [i]scan[/]."))
            return
        }
        val scan = scanService.getById(scanId)
        val probeAction = determineNextScanTarget(scan)
        stompService.toSite(scan.siteId, ReduxActions.SERVER_PROBE_ACTION, probeAction)
    }

    data class ProbeAction(val path: List<String>, val type: ProbeScanAction, val value: String)

    fun determineNextScanTarget(scan: Scan): ProbeAction {
        val traverseNodeValues = createTraverseNodes(scan.siteId).values
        traverseNodeValues.forEach { it.distance =  scan.nodeScanById[it.id]!!.distance }
        val traverseNodes = traverseNodeValues.filter { it.distance != null }
        val targetNode = traverseNodes.minBy {
            scan.nodeScanById[it.id]!!.status.level
        }!!

        val path = LinkedList<String>()
        path.add(targetNode.id)
        var currentNode = targetNode
        while (currentNode.distance != 0) {
            currentNode = currentNode.connections.find { it.distance == (currentNode.distance!! - 1) }!!
            path.add(0, currentNode.id)
        }
        val action = ProbeAction(path = path, type = ProbeScanAction.SCAN_CONNECTIONS, value = "burp")
        return action
    }
}