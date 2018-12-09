package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.Node
import org.n1.mainframe.backend.model.site.Service
import org.n1.mainframe.backend.model.site.Site
import org.n1.mainframe.backend.model.site.enums.ServiceType
import org.n1.mainframe.backend.model.site.NETWORK_ID
import org.n1.mainframe.backend.model.ui.AddNode
import org.n1.mainframe.backend.model.ui.MoveNode
import org.n1.mainframe.backend.repo.NodeRepo
import org.n1.mainframe.backend.util.createId
import org.n1.mainframe.backend.util.createServiceId
import sun.plugin.dom.exception.InvalidStateException

@org.springframework.stereotype.Service
class NodeService(
        val siteService: SiteService,
        val nodeRepo: NodeRepo
) {

    fun createNode(command: AddNode): Node {
        val id = createId("node", nodeRepo::findOne)
        val services = listOf( createOsService(command.siteId) )

        val node = Node(id, command.type, command.x, command.y, command.type.ice, services )
        nodeRepo.save(node)
        return node
    }

    private fun createOsService(siteId: String): Service {
        val site = siteService.getById(siteId)
        val nodes = getAll(site.nodes)
        val id = createServiceId(nodes, siteId)
        val networkId = nextFreeNetworkId( site, nodes )
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

    private fun nextFreeNetworkId(site: Site, nodes: List<Node>): String {
        val usedNetworkIds : Set<String> = HashSet(nodes.map{ node:Node -> node.services[0]!!.data[NETWORK_ID]!!  })

        for (i in 0 .. usedNetworkIds.size+1 ) {
            val candidate = String.format("%02d", i)
            if (!usedNetworkIds.contains(candidate)) {
                return candidate
            }
        }
        throw InvalidStateException("Failed to find a free network ID for site: ${site.id}")
    }

    fun getAll(nodeIds: List<String>): List<Node> {
        return nodeRepo.findByIdIn(nodeIds)
    }

    fun getById(nodeId: String) : Node {
        return nodeRepo.findOne(nodeId) ?: throw IllegalStateException("Node not found with id: ${nodeId}")
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


}