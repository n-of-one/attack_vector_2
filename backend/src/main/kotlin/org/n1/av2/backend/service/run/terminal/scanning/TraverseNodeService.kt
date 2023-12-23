package org.n1.av2.backend.service.run.terminal.scanning

import org.n1.av2.backend.entity.site.ConnectionEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.service.site.SiteService
import org.springframework.stereotype.Service

/**
 * TraverseNodes are used to calculate paths in sites. They are used for scanning and patrollers
 */
@Service
class TraverseNodeService(
    val sitePropertiesEntityService: SitePropertiesEntityService,
    val siteService: SiteService,
    val connectionEntityService: ConnectionEntityService
) {

    fun createTraverseNodes(siteId: String, nodes: List<Node>): Map<String, TraverseNode> {
        val connections = connectionEntityService.getAll(siteId)

        val traverseNodes = nodes.map { TraverseNode(nodeId = it.id, networkId = it.networkId, unhackedIce = it.unhackedIce) }
        val traverseNodesById = traverseNodes.map { it.nodeId to it }.toMap()
        connections.forEach {
            val from = traverseNodesById[it.fromId] ?: throw IllegalStateException("Node ${it.fromId} not found in ${siteId} in ${it.id}")
            val to = traverseNodesById[it.toId] ?: throw IllegalStateException("Node ${it.toId} not found in ${siteId} in ${it.id}")
            from.connections.add(to)
            to.connections.add(from)
        }
        return traverseNodesById
    }

    fun createTraverseNodesWithDistance(siteId: String, nodes: List<Node>, targetNodeId: String): Pair<TraverseNode, Map<String, TraverseNode>> {
        val startNode = startNode(siteId, nodes)
        val traverseNodesById = createTraverseNodes(siteId, nodes)

        val start: TraverseNode = traverseNodesById[startNode.id]!!
        val target: TraverseNode = traverseNodesById[targetNodeId]!!

        TraverseNode.removeIceBlockedNodes(target, traverseNodesById.values)
        start.fillDistanceFromHere(1)

        return Pair(start, traverseNodesById)
    }



    fun createTraverseNodesWithDistance(siteId: String, nodes: List<Node>): Map<String, TraverseNode> {
        val startNode = startNode(siteId, nodes)
        val traverseNodesById = createTraverseNodes(siteId, nodes)

        val start: TraverseNode = traverseNodesById[startNode.id]!!

        start.fillDistanceFromHere(1)

        return traverseNodesById
    }

    private fun startNode(siteId: String, nodes: List<Node>): Node {
        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        val startNode = siteService.findStartNode(siteProperties.startNodeNetworkId, nodes) ?: throw IllegalStateException("Invalid start node network ID")
        return startNode
    }
}