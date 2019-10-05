package org.n1.av2.backend.service.scan

import org.n1.av2.backend.model.db.run.Scan
import org.n1.av2.backend.service.site.ConnectionService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.service.site.SiteDataService
import org.n1.av2.backend.service.site.SiteService
import org.springframework.stereotype.Service

/**
 * TraverseNodes are used to calculate paths in sites. They are used for scanning and patrollers
 */
@Service
class TraverseNodeService(
        val siteDataService: SiteDataService,
        val siteService: SiteService,
        val nodeService: NodeService,
        val connectionService: ConnectionService
) {

    fun createTraverseNodesWithDistance(scan: Scan): Map<String, TraverseNode> {
        val traverseNodeById = createTraverseNodesWithoutDistance(scan.siteId)
        traverseNodeById.values.forEach { it.distance = scan.nodeScanById[it.id]!!.distance }
        return traverseNodeById
    }

    fun createTraverseNodesWithDistance(siteId: String): Map<String, TraverseNode> {
        val traverseNodesById = createTraverseNodesWithoutDistance(siteId)

        val siteData = siteDataService.getBySiteId(siteId)
        val nodes = nodeService.getAll(siteId)
        val startNodeId = siteService.findStartNode(siteData.startNodeNetworkId, nodes)?.id ?: throw IllegalStateException("Invalid start node network ID")

        val startTraverseNode = traverseNodesById[startNodeId]!!
        startTraverseNode.fillDistanceFromHere(1)

        return traverseNodesById
    }

    fun createTraverseNodesWithoutDistance(siteId: String): Map<String, TraverseNode> {
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


}

data class TraverseNode(val id: String,
                        var distance: Int? = null,
                        val connections: MutableSet<TraverseNode> = HashSet(),
                        var visited: Boolean = false) {

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

    fun fillDistanceFromHere(distance: Int) {
        this.distance = distance
        this.connections
                .filter { it.distance == null }
                .forEach { it.fillDistanceFromHere(distance + 1) }
    }

    fun traceToDistance(targetDistance: Int): TraverseNode? {
        if (this.visited) {
            return null
        }
        if (this.distance == targetDistance) {
            return this
        }
        this.visited = true

        return this.connections.firstOrNull { neighbour ->
            neighbour.traceToDistance(targetDistance) != null
        }
    }

}
