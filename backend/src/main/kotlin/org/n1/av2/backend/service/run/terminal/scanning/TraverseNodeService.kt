package org.n1.av2.backend.service.run.terminal.scanning

import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.site.ConnectionEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
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
    val nodeEntityService: NodeEntityService,
    val connectionEntityService: ConnectionEntityService
) {

    // It is more efficient if you have a run, as the run already contains the distances
    fun createTraverseNodesWithDistance(run: Run, nodes: List<Node>): Map<String, TraverseNode> {
        val traverseNodeById = createTraverseNodesWithoutDistance(run.siteId, nodes)
        traverseNodeById.values.forEach { it.distance = run.nodeScanById[it.id]!!.distance }
        return traverseNodeById
    }

    fun createTraverseNodesWithDistance(siteId: String, nodes: List<Node>): Map<String, TraverseNode> {
        val traverseNodesById = createTraverseNodesWithoutDistance(siteId, nodes)

        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        val startNodeId = siteService.findStartNode(siteProperties.startNodeNetworkId, nodes)?.id ?: throw IllegalStateException("Invalid start node network ID")

        val startTraverseNode = traverseNodesById[startNodeId]!!
        startTraverseNode.fillDistanceFromHere(1)

        return traverseNodesById
    }

    fun createTraverseNodesWithoutDistance(siteId: String, nodes: List<Node>): Map<String, TraverseNode> {
        val connections = connectionEntityService.getAll(siteId)

        val traverseNodes = nodes.map { TraverseNode(id = it.id, networkId = it.networkId) }
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
                        val networkId: String,
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
        return "${networkId}(${id}) Dist: ${distance} Connect count: ${connections.size}"
    }

    fun fillDistanceFromHere(distance: Int) {
        this.distance = distance
        val nextDistance = distance +1
        this.connections
                .filter { it.distance == null || it.distance!! > nextDistance }
                .forEach { it.fillDistanceFromHere(nextDistance) }
    }

    fun traceToDistance(targetDistance: Int): TraverseNode? {
        if (this.visited) {
            return null
        }
        if (this.distance == targetDistance) {
            return this
        }
        this.visited = true

        // trace back to the target going via the connection with the shortest path (lowest distance)
        return this.connections
                .sortedBy { it.distance }
                .first()
                .traceToDistance(targetDistance)
    }

}
