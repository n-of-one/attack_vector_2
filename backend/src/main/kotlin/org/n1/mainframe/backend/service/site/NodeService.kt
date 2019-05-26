package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.service.OsService
import org.n1.mainframe.backend.model.service.TextService
import org.n1.mainframe.backend.model.site.Node
import org.n1.mainframe.backend.model.site.Service
import org.n1.mainframe.backend.model.site.enums.ServiceType
import org.n1.mainframe.backend.model.ui.site.AddNode
import org.n1.mainframe.backend.model.ui.site.CommandAddService
import org.n1.mainframe.backend.model.ui.site.CommandRemoveService
import org.n1.mainframe.backend.model.ui.site.MoveNode
import org.n1.mainframe.backend.repo.NodeRepo
import org.n1.mainframe.backend.util.createId
import org.n1.mainframe.backend.util.createServiceId
import java.util.*

const val NODE_MIN_X = 35
const val NODE_MAX_X = 607 - 35

const val NODE_MIN_Y = 35
const val NODE_MAX_Y = 815 - 48 - 100


@org.springframework.stereotype.Service
class NodeService(
        val layoutService: LayoutService,
        val nodeRepo: NodeRepo
) {

    fun createNode(command: AddNode): Node {
        val id = createId("node", nodeRepo::findById)
        val siteId = command.siteId
        val nodes = getAll(siteId)
        val services = listOf(createOsService(siteId, nodes)).toMutableList()
        val networkId = nextFreeNetworkId(siteId, nodes)

        val node = Node(
                id = id,
                siteId = siteId,
                type = command.type,
                x = command.x,
                y = command.y,
                ice = command.type.ice,
                services = services,
                networkId = networkId)
        nodeRepo.save(node)
        return node
    }

    private fun createOsService(siteId: String, nodes: List<Node>): Service {
        val id = createServiceId(nodes, siteId)
        return OsService(id)
    }

    private fun createServiceId(nodes: List<Node>, siteId: String): String {
        val allServiceIds: List<String> = nodes.map { node -> node.services.map { service -> service.id } }.flatten()
        val findExisting = fun(candidate: String): String? {
            return allServiceIds.find { it == candidate }
        }
        return createServiceId(siteId, findExisting)
    }

    private fun nextFreeNetworkId(siteId: String, nodes: List<Node>): String {
        val usedNetworkIds: Set<String> = HashSet(nodes.map { node: Node -> node.networkId })

        for (i in 0..usedNetworkIds.size + 1) {
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

    fun getById(nodeId: String): Node {
        return nodeRepo.findById(nodeId).orElseThrow { throw IllegalStateException("Node not found with id: ${nodeId}") }
    }

    fun moveNode(command: MoveNode): Node {
        val node = getById(command.nodeId)
        val x = capX(command.x)
        val y = capY(command.y)

        val movedNode = node.copy(x = x, y = y)
        nodeRepo.save(movedNode)
        return movedNode
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

            val x = capX(40 * ((node.x + 20) / 40))
            val y = capY(40 * ((node.y + 20) / 40))
            val copy = node.copy(x = x, y = y)

            nodeRepo.save(copy)
        }

    }

    fun capX(x: Int): Int {
        return capRange(x, NODE_MIN_X, NODE_MAX_X)
    }

    fun capY(y: Int): Int {
        return capRange(y, NODE_MIN_Y, NODE_MAX_Y)
    }

    fun capRange(value: Int, lowerBound: Int, upperBound: Int): Int {
        val lowerCapped = Math.max(value, lowerBound)
        val totalCapped = Math.min(lowerCapped, upperBound)
        return totalCapped
    }

    fun getAllById(nodeIds: Collection<String>): List<Node> {
        return nodeRepo.findAllById(nodeIds).map { it }
    }

    fun findByNetworkId(siteId: String, networkId: String): Node? {
        val nodes = nodeRepo.findBySiteIdAndNetworkId(siteId, networkId)
        if (nodes.isNotEmpty()) {
            return nodes[0]
        } else {
            return null
        }
    }

    fun save(node: Node) {
        nodeRepo.save(node)
    }

    fun addService(command: CommandAddService): Service {
        val node = getById(command.nodeId)
        val service = createService(command.siteId, command.serviceType, node)
        node.services.add(service)
        nodeRepo.save(node)
        return service
    }

    private fun createService(siteId: String, serviceType: ServiceType, node: Node): Service {
        val nodes = getAll(siteId)
        val layer = node.services.size
        val id = createServiceId(nodes, siteId)

        return when (serviceType) {
            ServiceType.TEXT -> TextService(id, layer)
            else -> error("Unknown service type: ${serviceType}")
        }
    }

    data class ServiceRemoved(val node: Node, val serviceId: String, val nextLayer: Int)
    fun removeService(command: CommandRemoveService): ServiceRemoved? {
        val node = getById(command.nodeId)
        val toRemove = node.services.firstOrNull { it.id == command.serviceId } ?: return null
        node.services.remove(toRemove)
        node.services.sortBy { it.layer }
        node.services.forEachIndexed { layer, service -> service.layer = layer };
        nodeRepo.save(node)

        return ServiceRemoved(node, command.serviceId, toRemove.layer -1)
    }
}