package org.n1.av2.backend.service.scan

import org.n1.av2.backend.model.db.run.Scan
import org.n1.av2.backend.service.site.ConnectionService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.service.site.SiteDataService
import org.n1.av2.backend.service.site.SiteService
import org.springframework.stereotype.Service

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

        val siteData = siteDataService.getById(siteId)
        val nodes = nodeService.getAll(siteId)
        val startNodeId = siteService.findStartNode(siteData.startNodeNetworkId, nodes)?.id ?: throw IllegalStateException("Invalid start node network ID")

        val startTraverseNode = traverseNodesById[startNodeId]!!
        traverseAndSetDistance(startTraverseNode, 1)

        return traverseNodesById
    }

    private fun createTraverseNodesWithoutDistance(siteId: String): Map<String, TraverseNode> {
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

    private fun traverseAndSetDistance(node: TraverseNode, distance: Int) {
        node.distance = distance
        node.connections
                .filter { it.distance == null }
                .forEach { traverseAndSetDistance(it, distance + 1) }
    }

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
