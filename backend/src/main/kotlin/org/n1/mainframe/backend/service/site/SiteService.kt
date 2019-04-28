package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.NETWORK_ID
import org.n1.mainframe.backend.model.site.Node
import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.site.SiteFull
import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class SiteService(
        val stompService: StompService,
        val layoutService: LayoutService,
        val siteDataService: SiteDataService,
        val nodeService: NodeService,
        val connectionService: ConnectionService
) {

    fun createSite(name: String): String {
        val id = siteDataService.createId()
        siteDataService.create(id, name)
        layoutService.create(id)
        return id
    }


    fun getSiteFull(siteId: String): SiteFull {
        val siteData = siteDataService.getById(siteId)
        val layout = layoutService.getById(siteId)
        val nodes = nodeService.getAll(siteId)
        val startNodeId = findStartNode(siteData.startNodeNetworkId, nodes)?.id
        val connections = connectionService.getAll(siteId)

        return SiteFull(siteData, layout, nodes, connections, startNodeId)
    }

    fun findStartNode(startNodeNetworkId: String, nodes: List<Node>): Node? {
        return nodes.find { node -> node.services[0].data[NETWORK_ID] == startNodeNetworkId }
    }

    fun purgeAll() {
        siteDataService.findAll().forEach { siteData ->
            stompService.toSite(siteData.id, ReduxActions.SERVER_FORCE_DISCONNECT, NotyMessage("fatal", "Admin action", "Purging all sites."))
        }

        siteDataService.purgeAll()
        layoutService.purgeAll();
        nodeService.purgeAll()
        connectionService.purgeAll()
    }


    // --- --- --- //

    data class TraverseNode(val id: String,
                            var distance: Int? = null,
                            val connections: MutableSet<TraverseNode> = HashSet()) {

        override fun equals(other: Any?): Boolean {
            if (other is TraverseNode) { return other.id == this.id }
            return false
        }

        override fun hashCode(): Int { return this.id.hashCode() }
    }

    fun updateDistances(siteId: String) {
        val siteData = siteDataService.getById(siteId)
        val nodes = nodeService.getAll(siteId)
        val startNodeId = findStartNode(siteData.startNodeNetworkId, nodes)?.id ?: throw IllegalStateException("Invalid start node network ID")
        val connections = connectionService.getAll(siteId)

        val traverseNodes = nodes.map { TraverseNode(id = it.id) }
        val traverseNodesById = traverseNodes.map { it.id to it }.toMap()
        connections.forEach {
            val from = traverseNodesById[it.fromId] ?: throw IllegalStateException("Node ${it.fromId} not found in ${siteId} in ${it.id}")
            val to = traverseNodesById[it.toId] ?: throw IllegalStateException("Node ${it.toId} not found in ${siteId} in ${it.id}")
            from.connections.add(to)
            to.connections.add(from)
        }

        val startTraverseNode = traverseNodesById[startNodeId]!!
        setDistances(startTraverseNode, 0)
        nodes.forEach { it.distance = traverseNodesById[it.id]!!.distance }
        nodeService.updateNodes(nodes)
    }

    fun setDistances(node: TraverseNode, distance: Int) {
        node.distance = distance
        node.connections
                .filter { it.distance == null }
                .forEach { setDistances(it, distance + 1) }
    }


}