package org.n1.mainframe.backend.service.scan

import org.n1.mainframe.backend.model.hacker.HackerActivityType
import org.n1.mainframe.backend.model.hacker.HackerPresence
import org.n1.mainframe.backend.model.scan.NodeScan
import org.n1.mainframe.backend.model.scan.NodeScanType
import org.n1.mainframe.backend.model.scan.NodeStatus
import org.n1.mainframe.backend.model.scan.Scan
import org.n1.mainframe.backend.model.site.Node
import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.NotyType
import org.n1.mainframe.backend.model.ui.site.SiteFull
import org.n1.mainframe.backend.model.user.User
import org.n1.mainframe.backend.service.PrincipalService
import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.n1.mainframe.backend.service.site.ConnectionService
import org.n1.mainframe.backend.service.site.NodeService
import org.n1.mainframe.backend.service.site.SiteDataService
import org.n1.mainframe.backend.service.site.SiteService
import org.n1.mainframe.backend.service.user.HackerActivityService
import org.n1.mainframe.backend.service.user.HackerService
import org.n1.mainframe.backend.util.s
import org.springframework.stereotype.Service
import java.security.Principal
import java.util.*
import kotlin.collections.HashSet

/** This service deals with the action of scanning (as opposed to the actions performed on a scan). */
@Service
class ScanningService(val scanService: ScanService,
                      val siteDataService: SiteDataService,
                      val siteService: SiteService,
                      val stompService: StompService,
                      val nodeService: NodeService,
                      val connectionService: ConnectionService,
                      val hackerActivityService: HackerActivityService,
                      val hackerService: HackerService,
                      val principalService: PrincipalService) {

    data class ScanResponse(val scanId: String?, val message: NotyMessage?)

    fun scanSite(siteName: String): ScanResponse {
        val siteData = siteDataService.findByName(siteName) ?: return ScanResponse(null, NotyMessage(NotyType.ERROR, "Error", "Site '${siteName}' not found"))
        val nodeScans = createNodeScans(siteData.id)
        val scanId = scanService.createScan(siteData, nodeScans)
        return ScanResponse(scanId, null)
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


    data class ScanAndSite(val scan: Scan, val site: SiteFull, val users: List<HackerPresence>)

    fun enterScan(scanId: String) {
        hackerActivityService.startActivityScanning(scanId)

        val scan = scanService.getById(scanId)
        val siteFull = siteService.getSiteFull(scan.siteId)
        val userPresence = hackerService.getUserPresence(HackerActivityType.SCANNING, scan.id)


        val scanAndSite = ScanAndSite(scan, siteFull, userPresence)
        stompService.toUser(ReduxActions.SERVER_SCAN_FULL, scanAndSite)
    }


    private fun reportNodeNotFound(networkId: String) {
        if (networkId.length == 1) {
            stompService.terminalReceive("Node [ok]${networkId}[/] not found. Did you mean: [u]scan [ok]0${networkId}[/] ?")

        }
        else {
            stompService.terminalReceive("Node [ok]${networkId}[/] not found.")
        }

    }

    fun launchProbeAtNode(scanId: String, networkId: String) {
        val scan = scanService.getById(scanId)
        val node = nodeService.findByNetworkId(scan.siteId, networkId)
        if (node == null) {
            reportNodeNotFound(networkId)
            return
        }

        val traverseNodesById = createTraverseNodesWithDistance(scan)
        val targetNode = traverseNodesById[node.id]!!
        val status = scan.nodeScanById[targetNode.id]!!.status
        if (status == NodeStatus.UNDISCOVERED) {
            reportNodeNotFound(networkId)
            return
        }
        val scanType = determineNodeScanType(status) ?: NodeScanType.SCAN_NODE_DEEP
        val path = createNodePath(targetNode)
        val userId = principalService.get().userId
        val probeAction = ProbeAction(userId = userId, path = path, scanType = scanType, autoScan = false)
        stompService.toSite(scan.siteId, ReduxActions.SERVER_PROBE_LAUNCH, probeAction)
    }


    fun autoScan(scanId: String) {
        launchProbe(scanId, true)
    }

    fun launchProbe(scanId: String, autoScan: Boolean) {
        val scan = scanService.getById(scanId)
        val probeAction = createProbeAction(scan, autoScan)
        if (probeAction != null) {
            stompService.toSite(scan.siteId, ReduxActions.SERVER_PROBE_LAUNCH, probeAction)
        } else {
            stompService.terminalReceive("Scan complete.")
        }
    }

    data class ProbeAction(val userId: String, val path: List<String>, val scanType: NodeScanType, val autoScan: Boolean)

    fun createProbeAction(scan: Scan, autoScan: Boolean): ProbeAction? {
        val targetNode = findProbeTarget(scan)
        val status = scan.nodeScanById[targetNode.id]!!.status
        val scanType = determineNodeScanType(status) ?: return null
        val path = createNodePath(targetNode)
        val userId = principalService.get().userId
        return ProbeAction(userId = userId, path = path, scanType = scanType, autoScan = autoScan)
    }

    private fun determineNodeScanType(status: NodeStatus): NodeScanType? {
        return when (status) {
            NodeStatus.DISCOVERED -> NodeScanType.SCAN_NODE_INITIAL
            NodeStatus.TYPE -> NodeScanType.SCAN_CONNECTIONS
            NodeStatus.CONNECTIONS -> NodeScanType.SCAN_NODE_DEEP
            NodeStatus.SERVICES -> null
            NodeStatus.UNDISCOVERED -> error("Cannot scan a node that has not yet been discovered.")
        }
    }

    private fun createNodePath(targetNode: TraverseNode): LinkedList<String> {
        val path = LinkedList<String>()
        path.add(targetNode.id)
        var currentNode = targetNode
        while (currentNode.distance != 0) {
            currentNode = currentNode.connections.find { it.distance == (currentNode.distance!! - 1) }!!
            path.add(0, currentNode.id)
        }
        return path
    }

    /**
     * Of all nodes that are know to the players, find the one of which the least is known (lowest scan status level).
     */
    private fun findProbeTarget(scan: Scan): TraverseNode {
        val traverseNodeValues = createTraverseNodesWithDistance(scan).values
        val traverseNodes = traverseNodeValues.filter { scan.nodeScanById[it.id]!!.status != NodeStatus.UNDISCOVERED }
        val distanceSortedNodes = traverseNodes.sortedBy { it.distance }
        val targetNode = distanceSortedNodes.minBy {
            scan.nodeScanById[it.id]!!.status.level
        }!!
        return targetNode
    }

    private fun createTraverseNodesWithDistance(scan: Scan): Map<String, TraverseNode> {
        val traverseNodeById = createTraverseNodes(scan.siteId)
        traverseNodeById.values.forEach { it.distance = scan.nodeScanById[it.id]!!.distance }
        return traverseNodeById
    }

    //---//

    fun probeArrive(scanId: String, nodeId: String, action: NodeScanType) {
        val scan = scanService.getById(scanId)
        val nodeScan = scan.nodeScanById[nodeId] ?: throw IllegalStateException("Node to scan ${nodeId} not part of ${scan.siteId}")
        val node = nodeService.getById(nodeId)

        when (action) {
            NodeScanType.SCAN_NODE_INITIAL -> probeScanInitial(scan, node, nodeScan)
            NodeScanType.SCAN_CONNECTIONS -> probeScanConnection(scan, node, nodeScan)
            NodeScanType.SCAN_NODE_DEEP -> probeScanDeep(scan, node, nodeScan)
        }
    }

    data class ProbeResultInitial(val nodeId: String, val newStatus: NodeStatus)

    fun probeScanInitial(scan: Scan, node: Node, nodeScan: NodeScan) {
        if (nodeScan.status != NodeStatus.DISCOVERED) {
            stompService.terminalReceive("Scanning node ${node.networkId} did not find anything new.")
            return
        }
        stompService.terminalReceive("Scanned node ${node.networkId} - discovered type")

        nodeScan.status = NodeStatus.TYPE
        scanService.save(scan)
        stompService.toScan(scan.id, ReduxActions.SERVER_UPDATE_NODE_STATUS, ProbeResultInitial(node.id, nodeScan.status))
    }

    private fun probeScanConnection(scan: Scan, node: Node, nodeScan: NodeScan) {
        if (nodeScan.status != NodeStatus.TYPE) {
            stompService.terminalReceive("Scanning node ${node.networkId} did not find new connections.")
            return
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

        val connectionsFromDiscoveredNodes = discoveredNodeIds.flatMap{connectionService.findByNodeId(it)  }

        val extraDiscoveredConnections = connectionsFromDiscoveredNodes
                .filter { allDiscoveredNodes.contains(it.fromId) && allDiscoveredNodes.contains(it.toId) }
                .map { it.id }

        discoveredConnectionIds.addAll(extraDiscoveredConnections)


        scanService.save(scan)
        stompService.toScan(scan.id, ReduxActions.SERVER_UPDATE_NODE_STATUS, ProbeResultInitial(node.id, nodeScan.status))

        data class ProbeResultConnections(val nodeIds: List<String>, val connectionIds: Collection<String>)
        stompService.toScan(scan.id, ReduxActions.SERVER_DISCOVER_NODES, ProbeResultConnections(discoveredNodeIds, discoveredConnectionIds))

        stompService.terminalReceive( "Scanned node ${node.networkId} - discovered ${discoveredNodeIds.size} ${"node".s(discoveredNodeIds.size)}")

    }

    private fun probeScanDeep(scan: Scan, node: Node, nodeScan: NodeScan) {
        if (nodeScan.status != NodeStatus.CONNECTIONS) {
            stompService.terminalReceive("Scanning node ${node.networkId} did not find anything.")
            return
        }

        val iceMessage = if (node.ice) " ! Ice detected" else ""
        stompService.terminalReceive("Scanned node ${node.networkId} - discovered ${node.services.size} ${"service".s(node.services.size)}${iceMessage}")

        nodeScan.status = NodeStatus.SERVICES
        scanService.save(scan)
        stompService.toScan(scan.id, ReduxActions.SERVER_UPDATE_NODE_STATUS, ProbeResultInitial(node.id, nodeScan.status))
    }

    data class ScanOverViewLine(val scanId: String, val siteName: String, val complete: Boolean, val siteId: String)
    fun scansOfPlayer(principal: Principal): Collection<ScanOverViewLine> {
        val scans = scanService.getAll(principal)
        return scans.map {scan ->
            val site = siteDataService.getById(scan.siteId)
            val complete = (scan.nodeScanById.values.find { it.status != NodeStatus.SERVICES } == null)
            ScanOverViewLine(scan.id, site.name, complete, site.id)
        }
    }

    fun shareScan(scanId: String, user: User) {
        if (scanService.hasUserScan(user, scanId)) {
            stompService.terminalReceive("[info]${user.name}[/] already has this scan.")
            return
        }
        scanService.createUserScan(scanId, user)
        stompService.terminalReceive("Shared scan with [info]${user.name}[/].")

        val myUserName = principalService.get().user.name

        val scan = scanService.getById(scanId)
        val siteData = siteDataService.getById(scan.siteId)

        stompService.toUser(user.id, NotyMessage(NotyType.NEUTRAL, myUserName, "Scan shared for: ${siteData.name}"))
    }


}