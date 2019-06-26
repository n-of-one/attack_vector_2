package org.n1.av2.backend.service.site

import org.n1.av2.backend.model.db.service.IcePasswordService
import org.n1.av2.backend.model.db.service.OsService
import org.n1.av2.backend.model.db.service.Service
import org.n1.av2.backend.model.db.service.TextService
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.db.site.enums.ServiceType
import org.n1.av2.backend.model.ui.*
import org.n1.av2.backend.repo.NodeRepo
import org.n1.av2.backend.service.ThemeService
import org.n1.av2.backend.util.createId
import org.n1.av2.backend.util.createServiceId
import java.util.*

const val NODE_MIN_X = 35
const val NODE_MAX_X = 607 - 35

const val NODE_MIN_Y = 35
const val NODE_MAX_Y = 815 - 48 - 100


@org.springframework.stereotype.Service
class NodeService(
        val layoutService: LayoutService,
        val nodeRepo: NodeRepo,
        val themeService: ThemeService
) {

    fun createNode(command: AddNode): Node {
        val id = createId("node", nodeRepo::findById)
        val siteId = command.siteId
        val nodes = getAll(siteId)
        val networkId = nextFreeNetworkId(siteId, nodes)
        val services = listOf(createOsService(siteId, nodes)).toMutableList()

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
        val name = themeService.getDefaultName(ServiceType.OS)
        return OsService(id, name)
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

    fun findByNetworkId(siteId: String, networkId: String): Node? {
        return nodeRepo.findBySiteIdAndNetworkId(siteId, networkId) ?: nodeRepo.findBySiteIdAndNetworkIdIgnoreCase(siteId, networkId)
    }

    fun save(node: Node) {
        nodeRepo.save(node)
    }

    fun addService(command: AddServiceCommand): Service {
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

        val defaultName = themeService.getDefaultName(serviceType)

        return when (serviceType) {
            ServiceType.TEXT -> TextService(id, layer, defaultName)
            ServiceType.ICE_PASSWORD -> IcePasswordService(id, layer, defaultName)
            else -> error("Unknown service type: ${serviceType}")
        }
    }

    data class ServicesUpdated(val node: Node, val serviceId: String?)

    fun removeService(command: RemoveServiceCommand): ServicesUpdated? {
        val node = getById(command.nodeId)
        val toRemove = node.services.firstOrNull { it.id == command.serviceId } ?: return null
        node.services.remove(toRemove)
        node.services.sortBy { it.layer }
        node.services.forEachIndexed { layer, service -> service.layer = layer };
        nodeRepo.save(node)

        val newFocusNode = node.services[toRemove.layer - 1]

        return ServicesUpdated(node, newFocusNode.id)
    }

    fun swapServices(command: SwapServiceCommand): ServicesUpdated? {
        val node = getById(command.nodeId)
        val fromService = node.services.find { it.id == command.fromId } ?: return null
        val toService = node.services.find { it.id == command.toId } ?: return null

        val fromLayer = fromService.layer
        val toLayer = toService.layer
        fromService.layer = toLayer
        toService.layer = fromLayer
        node.services.sortBy { it.layer }

        nodeRepo.save(node)

        return ServicesUpdated(node, null)
    }

}