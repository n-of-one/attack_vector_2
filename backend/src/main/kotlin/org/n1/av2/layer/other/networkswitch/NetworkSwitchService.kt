package org.n1.av2.layer.other.networkswitch

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.layer.other.networkswitch.ConnectionStatus.OPEN
import org.n1.av2.layer.other.networkswitch.ConnectionStatus.PENDING
import org.n1.av2.layer.other.networkswitch.ConnectionStatus.DROPPED
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ConnectionService.TerminalReceive
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.run.RunService
import org.n1.av2.run.terminal.TERMINAL_MAIN
import org.n1.av2.site.entity.Connection
import org.n1.av2.site.entity.ConnectionEntityService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service

// Ordered from least to most restrictive: a connection that is a route of two network switches gets the most restrictive status.
enum class ConnectionStatus {
    OPEN,
    PENDING,  // a network switch has not chosen yet: no moving, but scans and remote scripts pass
    DROPPED,  // a network switch chose another route: nothing passes, except normal scans
}

fun Connection.connects(nodeId1: String, nodeId2: String) =
    (fromId == nodeId1 && toId == nodeId2) || (fromId == nodeId2 && toId == nodeId1)

private fun routeStatus(layer: NetworkSwitchLayer, routeNodeId: String) = when (layer.lockedRouteNodeId) {
    null -> PENDING
    routeNodeId -> OPEN
    else -> DROPPED
}

fun calculateConnectionStatuses(nodes: List<Node>, connections: List<Connection>): Map<String, ConnectionStatus> {
    val statusById = connections.associate { it.id to OPEN }.toMutableMap()
    nodes.forEach { node ->
        node.layers.filterIsInstance<NetworkSwitchLayer>().forEach { layer ->
            layer.routes.forEach { route ->
                val connection = connections.find { it.connects(node.id, route.nodeId) } ?: return@forEach
                statusById[connection.id] = maxOf(statusById[connection.id]!!, routeStatus(layer, route.nodeId))
            }
        }
    }
    return statusById
}

/**
 * Status of the connection between two neighbouring nodes. Only network switches in these two nodes can affect it.
 */
fun calculateConnectionStatus(node1: Node, node2: Node): ConnectionStatus {
    return maxOf(statusBySwitchesIn(node1, node2.id), statusBySwitchesIn(node2, node1.id))
}

private fun statusBySwitchesIn(switchNode: Node, otherNodeId: String): ConnectionStatus {
    return switchNode.layers
        .filterIsInstance<NetworkSwitchLayer>()
        .filter { it.routeFor(otherNodeId) != null }
        .maxOfOrNull { routeStatus(it, otherNodeId) }
        ?: OPEN
}

fun calculateReachableNodeIds(
    startNodeId: String,
    connections: List<Connection>,
    statusById: Map<String, ConnectionStatus>,
    pendingIsOpen: Boolean,
): Set<String> {
    val passable = connections.filter { connection ->
        val status = statusById[connection.id] ?: OPEN
        status == OPEN || (pendingIsOpen && status == PENDING)
    }
    val reached = mutableSetOf(startNodeId)
    val toVisit = ArrayDeque(listOf(startNodeId))
    while (toVisit.isNotEmpty()) {
        val nodeId = toVisit.removeFirst()
        passable.forEach { connection ->
            val other = when (nodeId) {
                connection.fromId -> connection.toId
                connection.toId -> connection.fromId
                else -> null
            }
            if (other != null && reached.add(other)) toVisit.add(other)
        }
    }
    return reached
}

private const val CUT_OFF_MESSAGE = "Carrier lost: route to entry node collapsed. Uplink terminated."

@Service
class NetworkSwitchService(
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val connectionService: ConnectionService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val runService: RunService,
) {

    fun hack(layerId: String, arguments: List<String>) {
        val node = nodeEntityService.findByLayerId(layerId)
        val layer = node.getLayerById(layerId) as NetworkSwitchLayer
        val nodes = nodeEntityService.findBySiteId(node.siteId)
        val networkIdByNodeId = nodes.associate { it.id to it.networkId }

        val lockedRoute = layer.lockedRouteNodeId?.let { layer.routeFor(it) }
        if (lockedRoute != null) {
            val lockedText = "[warn]Switch locked.[/] Route fixed to [b]${lockedRoute.name}[/] → [ok]${networkIdByNodeId[lockedRoute.nodeId]}[/]."
            connectionService.replyTerminalReceive(listOfNotNull(lockedText, coreForUnlockingText(layer, node.siteId)))
            return
        }

        val choice = arguments.firstOrNull()
        if (choice == null) {
            replyChoices(layer, networkIdByNodeId, "Routing table:")
            return
        }

        val route = layer.routes.find { it.name.equals(choice, ignoreCase = true) }
            ?: layer.routes.find { networkIdByNodeId[it.nodeId].equals(choice, ignoreCase = true) }
            ?: return replyChoices(layer, networkIdByNodeId, "[error]route error[/] no route named [b]${choice}[/]. Routing table:")

        layer.lockedRouteNodeId = route.nodeId
        nodeEntityService.save(node)
        lockChanged(node, layer)
    }

    fun lockChanged(node: Node, layer: NetworkSwitchLayer) {
        val lockedRoute = layer.lockedRouteNodeId?.let { layer.routeFor(it) }
        val line = if (lockedRoute != null) {
            val networkIdByNodeId = nodeEntityService.findBySiteId(node.siteId).associate { it.id to it.networkId }
            val dropped = layer.routes.filter { it != lockedRoute }.joinToString(", ") { "[ok]${networkIdByNodeId[it.nodeId]}[/]" }
            "[warn]Topology change[/] switch [ok]${node.networkId}[/] locked to [b]${lockedRoute.name}[/]. Dropped routes: ${dropped}"
        } else {
            "[warn]Topology change[/] switch [ok]${node.networkId}[/] unlocked. Routes back in standby."
        }
        connectionService.toSite(node.siteId, ServerActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, arrayOf(line)))
        connectionService.toSite(node.siteId, ServerActions.SERVER_LAYER_CHANGED, "nodeId" to node.id, "layer" to layer)
        sendConnectionStatuses(node.siteId)
        if (lockedRoute == null) return

        disconnectHackersCutOffFromStart(node.siteId)
        // The node behind the route could only be known as unconnectable, because scans do not go past a dropped route.
        runService.makeNeighborConnectable(node, lockedRoute.nodeId)
    }

    fun disconnectHackersCutOffFromStart(siteId: String) {
        val reachable = reachableNodeIds(siteId, pendingIsOpen = true)
        if (reachable.isEmpty()) return // no start node
        hackerStateEntityService.findAllHackersInSite(siteId)
            .filter { it.activity == HackerActivity.INSIDE && it.currentNodeId !in reachable }
            .forEach { disconnectCutOff(it) }
    }

    fun disconnectCutOff(hackerState: HackerState) {
        runService.hackerDisconnect(hackerState, CUT_OFF_MESSAGE)
    }

    private fun coreForUnlockingText(layer: NetworkSwitchLayer, siteId: String): String? {
        val coreLayerId = layer.coreLayerId ?: return null
        val coreNode = nodeEntityService.findByLayerId(coreLayerId)
        if (coreNode.siteId == siteId) return "Lock managed by core in node [ok]${coreNode.networkId}"
        val siteName = sitePropertiesEntityService.getBySiteId(coreNode.siteId).name
        return "Lock managed by remote core in node [ok]${coreNode.networkId}[/] in site: [info]${siteName}"
    }

    /**
     * Removes routes whose connection no longer exists, for example because the node at the other end was deleted.
     */
    fun removeRoutesWithoutConnection(siteId: String) {
        val connections = connectionEntityService.getAll(siteId)
        nodeEntityService.findBySiteId(siteId).forEach { node ->
            var changed = false
            node.layers.filterIsInstance<NetworkSwitchLayer>().forEach { layer ->
                val removed = layer.routes.removeIf { route -> connections.none { it.connects(node.id, route.nodeId) } }
                if (removed) {
                    changed = true
                    if (layer.lockedRouteNodeId != null && layer.routeFor(layer.lockedRouteNodeId!!) == null) layer.lockedRouteNodeId = null
                }
            }
            if (changed) nodeEntityService.save(node)
        }
    }

    class UnlockedNetworkSwitch(val siteId: String, val networkId: String)

    /**
     * Unlocks all locked network switches (in any site) that are linked to this core.
     */
    fun unlockSwitchesForCore(coreLayerId: String): List<UnlockedNetworkSwitch> {
        return nodeEntityService.findAllNodesLinkedToCore(coreLayerId).flatMap { node ->
            val unlockedLayers = node.layers
                .filterIsInstance<NetworkSwitchLayer>()
                .filter { it.coreLayerId == coreLayerId && it.lockedRouteNodeId != null }
            if (unlockedLayers.isEmpty()) return@flatMap emptyList()

            unlockedLayers.forEach { it.lockedRouteNodeId = null }
            nodeEntityService.save(node)

            val lines = arrayOf("[warn]Topology change[/] switch [ok]${node.networkId}[/] unlocked by core. Routes back in standby.")
            connectionService.toSite(node.siteId, ServerActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, lines))
            unlockedLayers.forEach { layer ->
                connectionService.toSite(node.siteId, ServerActions.SERVER_LAYER_CHANGED, "nodeId" to node.id, "layer" to layer)
            }
            sendConnectionStatuses(node.siteId)

            unlockedLayers.map { UnlockedNetworkSwitch(node.siteId, node.networkId) }
        }
    }

    private fun replyChoices(layer: NetworkSwitchLayer, networkIdByNodeId: Map<String, String>, header: String) {
        val choices = layer.routes.map { route ->
            "  [b]hack [primary]${layer.level}[/] ${route.name}[/]  → [ok]${networkIdByNodeId[route.nodeId]}[/]"
        }
        val warning = "[warn]warning[/] switch firmware locks after first route commit. Uncommitted routes will be dropped."
        connectionService.replyTerminalReceive(listOf(header) + choices + warning)
    }

    fun sendConnectionStatuses(siteId: String) {
        val statuses = connectionStatuses(siteId)
        connectionService.toSite(siteId, ServerActions.SERVER_CONNECTIONS_UPDATED, "connectionStatusById" to statuses)
    }

    fun connectionStatuses(siteId: String): Map<String, ConnectionStatus> {
        return calculateConnectionStatuses(nodeEntityService.findBySiteId(siteId), connectionEntityService.getAll(siteId))
    }

    /**
     * Reachable from the start node, ignoring ICE. Used for everything that is not a regular move to a neighbouring node.
     */
    fun isReachable(siteId: String, targetNodeId: String, pendingIsOpen: Boolean): Boolean {
        return reachableNodeIds(siteId, pendingIsOpen).contains(targetNodeId)
    }

    private fun reachableNodeIds(siteId: String, pendingIsOpen: Boolean): Set<String> {
        val nodes = nodeEntityService.findBySiteId(siteId)
        val connections = connectionEntityService.getAll(siteId)
        val startNetworkId = sitePropertiesEntityService.getBySiteId(siteId).startNodeNetworkId
        val startNode = nodes.find { it.networkId == startNetworkId } ?: return emptySet()

        val statusById = calculateConnectionStatuses(nodes, connections)
        return calculateReachableNodeIds(startNode.id, connections, statusById, pendingIsOpen)
    }

    /**
     * @return an error message if a network switch blocks moving from [fromNode] to its neighbour [toNode], otherwise null.
     */
    fun checkMove(fromNode: Node, toNode: Node): String? {
        return when (calculateConnectionStatus(fromNode, toNode)) {
            OPEN -> null
            DROPPED -> "[error]route down[/] route to [ok]${toNode.networkId}[/] was dropped by a network switch."
            PENDING -> "[warn b]blocked[/] route to [ok]${toNode.networkId}[/] held in standby by a network switch."
        }
    }

}
