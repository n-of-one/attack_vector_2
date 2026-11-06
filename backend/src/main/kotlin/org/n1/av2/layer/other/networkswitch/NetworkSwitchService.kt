package org.n1.av2.layer.other.networkswitch

import org.n1.av2.layer.other.networkswitch.ConnectionStatus.OPEN
import org.n1.av2.layer.other.networkswitch.ConnectionStatus.PENDING
import org.n1.av2.layer.other.networkswitch.ConnectionStatus.SEVERED
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ConnectionService.TerminalReceive
import org.n1.av2.platform.connection.ServerActions
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
    SEVERED,  // a network switch chose another route: nothing passes, except normal scans
}

fun Connection.connects(nodeId1: String, nodeId2: String) =
    (fromId == nodeId1 && toId == nodeId2) || (fromId == nodeId2 && toId == nodeId1)

fun calculateConnectionStatuses(nodes: List<Node>, connections: List<Connection>): Map<String, ConnectionStatus> {
    val statusById = connections.associate { it.id to OPEN }.toMutableMap()
    nodes.forEach { node ->
        node.layers.filterIsInstance<NetworkSwitchLayer>().forEach { layer ->
            layer.routes.forEach { route ->
                val connection = connections.find { it.connects(node.id, route.nodeId) } ?: return@forEach
                val status = when (layer.chosenNodeId) {
                    null -> PENDING
                    route.nodeId -> OPEN
                    else -> SEVERED
                }
                statusById[connection.id] = maxOf(statusById[connection.id]!!, status)
            }
        }
    }
    return statusById
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

@Service
class NetworkSwitchService(
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val connectionService: ConnectionService,
) {

    fun hack(layerId: String, arguments: List<String>) {
        val node = nodeEntityService.findByLayerId(layerId)
        val layer = node.getLayerById(layerId) as NetworkSwitchLayer
        val nodes = nodeEntityService.findBySiteId(node.siteId)
        val networkIdByNodeId = nodes.associate { it.id to it.networkId }

        val chosenRoute = layer.chosenNodeId?.let { layer.routeFor(it) }
        if (chosenRoute != null) {
            connectionService.replyTerminalReceive(
                "[warn]Switch locked.[/] Route fixed to [b]${chosenRoute.name}[/] → [ok]${networkIdByNodeId[chosenRoute.nodeId]}[/]."
            )
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

        layer.chosenNodeId = route.nodeId
        nodeEntityService.save(node)

        val severed = layer.routes.filter { it != route }.joinToString(", ") { "[ok]${networkIdByNodeId[it.nodeId]}[/]" }
        val lines = arrayOf("[warn]Topology change[/] switch [ok]${node.networkId}[/] locked to [b]${route.name}[/]. Dropped routes: ${severed}")
        connectionService.toSite(node.siteId, ServerActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, lines))
        connectionService.toSite(node.siteId, ServerActions.SERVER_LAYER_CHANGED, "nodeId" to node.id, "layer" to layer)
        sendConnectionStatuses(node.siteId)
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
        val nodes = nodeEntityService.findBySiteId(siteId)
        val connections = connectionEntityService.getAll(siteId)
        val startNetworkId = sitePropertiesEntityService.getBySiteId(siteId).startNodeNetworkId
        val startNode = nodes.find { it.networkId == startNetworkId } ?: return false

        val statusById = calculateConnectionStatuses(nodes, connections)
        return calculateReachableNodeIds(startNode.id, connections, statusById, pendingIsOpen).contains(targetNodeId)
    }

    /**
     * @return an error message if a network switch blocks moving from [fromNodeId] to its neighbour [toNode], otherwise null.
     */
    fun checkMove(fromNodeId: String, toNode: Node): String? {
        val nodes = nodeEntityService.findBySiteId(toNode.siteId)
        val connections = connectionEntityService.getAll(toNode.siteId)
        val connection = connections.find { it.connects(fromNodeId, toNode.id) } ?: return null

        return when (calculateConnectionStatuses(nodes, connections)[connection.id]) {
            null, OPEN -> null
            SEVERED -> "[error]route down[/] route to [ok]${toNode.networkId}[/] was dropped by a network switch."
            PENDING -> "[warn b]blocked[/] route to [ok]${toNode.networkId}[/] held in standby by a network switch."
        }
    }

}
