package org.n1.av2.run.scanning

import org.n1.av2.site.entity.ConnectionEntityService
import org.n1.av2.site.entity.Node
import org.springframework.stereotype.Service

/**
 * TraverseNodes are used to calculate paths in sites. They are used for scanning and patrollers
 */
@Service
class TraverseNodeService(
    val connectionEntityService: ConnectionEntityService
) {

    fun createTraverseNodes(siteId: String, nodes: List<Node>): Map<String, TraverseNode> {
        val connections = connectionEntityService.getAll(siteId)

        val traverseNodes = nodes.map { TraverseNode(nodeId = it.id, networkId = it.networkId, unhackedIce = it.unhackedIce) }
        val traverseNodesById = traverseNodes.associate { it.nodeId to it }
        connections.forEach {
            val from = traverseNodesById[it.fromId] ?: throw IllegalStateException("Node ${it.fromId} not found in ${siteId} in ${it.id}")
            val to = traverseNodesById[it.toId] ?: throw IllegalStateException("Node ${it.toId} not found in ${siteId} in ${it.id}")
            from.connections.add(to)
            to.connections.add(from)
        }
        return traverseNodesById
    }

    fun createTraverseNodesWithDistance(siteId: String,
                                        startNodeId: String,
                                        nodes: List<Node>,
                                        targetNodeId: String,
                                        iceNodeIdToIgnore: String? = null): Pair<TraverseNode, Map<String, TraverseNode>> {
        val traverseNodesById = createTraverseNodes(siteId, nodes)

        val start: TraverseNode = traverseNodesById[startNodeId]!!
        val target: TraverseNode = traverseNodesById[targetNodeId]!!

        TraverseNode.removeIceBlockedNodes(target, traverseNodesById.values, iceNodeIdToIgnore)
        start.fillDistanceFromHere(1)

        return Pair(start, traverseNodesById)
    }

    fun createTraverseNodesWithDistance(siteId: String, startNodeId: String, nodes: List<Node>): Map<String, TraverseNode> {
        val traverseNodesById = createTraverseNodes(siteId, nodes)
        val start: TraverseNode = traverseNodesById[startNodeId]!!

        start.fillDistanceFromHere(1)

        return traverseNodesById
    }

}
