package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.NETWORK_ID
import org.n1.mainframe.backend.model.site.Node
import org.n1.mainframe.backend.model.site.Service
import org.n1.mainframe.backend.model.site.enums.ServiceType
import org.n1.mainframe.backend.model.ui.site.command.AddNode
import org.n1.mainframe.backend.model.ui.site.command.MoveNode
import org.n1.mainframe.backend.repo.NodeRepo
import org.n1.mainframe.backend.util.createId
import org.n1.mainframe.backend.util.createServiceId


@org.springframework.stereotype.Service
class NodeService(
        val layoutService: LayoutService,
        val nodeRepo: NodeRepo
) {

    fun createNode(command: AddNode): Node {
        val id = createId("node", nodeRepo::findById)
        val services = listOf( createOsService(command.siteId) )

        val node = Node(
                id = id,
                siteId = command.siteId,
                type = command.type,
                x = command.x,
                y = command.y,
                ice = command.type.ice,
                distance = null,
                services = services )
        nodeRepo.save(node)
        return node
    }

    private fun createOsService(siteId: String): Service {
        val nodes = getAll(siteId)
        val id = createServiceId(nodes, siteId)
        val networkId = nextFreeNetworkId( siteId, nodes )
        val data = mapOf(NETWORK_ID to networkId)

        return Service(id, ServiceType.OS, 0, data)
    }

    private fun createServiceId(nodes: List<Node>, siteId: String): String {
        val allServiceIds: List<String> = nodes.map { node -> node.services.map { service -> service.id } }.flatten()
        val findExisting = fun(candidate: String): String? {
            return allServiceIds.find { it == candidate }
        }
        return createServiceId(siteId, findExisting)
    }

    private fun nextFreeNetworkId(siteId: String, nodes: List<Node>): String {
        val usedNetworkIds : Set<String> = HashSet(nodes.map{ node:Node -> node.services[0].data[NETWORK_ID]!!  })

        for (i in 0 .. usedNetworkIds.size+1 ) {
            val candidate = String.format("%02d", i)
            if (!usedNetworkIds.contains(candidate)) {
                return candidate
            }
        }
        throw IllegalStateException("Failed to find a free network ID for site: ${siteId}")
    }

    fun getAll(siteId: String): List<Node> {
        return nodeRepo.findBySiteId(siteId)
    }

    fun getById(nodeId: String) : Node {
        return nodeRepo.findById(nodeId).orElseThrow { throw IllegalStateException("Node not found with id: ${nodeId}") }
    }

    fun moveNode(command: MoveNode) {
        val node = getById(command.nodeId)
        val movedNode = node.copy(x = command.x, y = command.y)
        nodeRepo.save(movedNode)
    }

    fun purgeAll() {
        nodeRepo.deleteAll()
    }

    fun deleteNode(nodeId: String) {
        val node = getById(nodeId)
        nodeRepo.delete(node)
    }

    fun snap(nodeIds: List<String>) {
        nodeIds.forEach { nodeId ->
            val node = getById(nodeId)
            node.x = 40 * ((node.x + 20) / 40)
            node.y = 40 * ((node.y + 20) / 40)
            nodeRepo.save(node)
        }
    }

    fun updateNodes(nodes: List<Node>) {
        nodes.forEach { nodeRepo.save(it) }
    }


}