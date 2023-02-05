package org.n1.av2.backend.service.site

import org.n1.av2.backend.model.db.layer.*
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.db.site.enums.LayerType
import org.n1.av2.backend.model.ui.*
import org.n1.av2.backend.repo.NodeRepo
import org.n1.av2.backend.service.ThemeService
import org.n1.av2.backend.util.createId
import org.n1.av2.backend.util.createLayerId

const val NODE_MIN_X = 35
const val NODE_MAX_X = 607 - 35

const val NODE_MIN_Y = 35
const val NODE_MAX_Y = 815 - 48 - 100


@org.springframework.stereotype.Service
class NodeService(
        val nodeRepo: NodeRepo,
        val themeService: ThemeService
) {

    fun createNode(command: AddNode): Node {
        val id = createId("node", nodeRepo::findById)
        val siteId = command.siteId
        val nodes = getAll(siteId)
        val networkId = nextFreeNetworkId(siteId, nodes)
        val layers = mutableListOf(createOsLayer(id))

        val node = Node(
                id = id,
                siteId = siteId,
                type = command.type,
                x = command.x,
                y = command.y,
                ice = command.type.ice,
                layers = layers,
                networkId = networkId)
        nodeRepo.save(node)
        return node
    }

    private fun createOsLayer(nodeId: String): Layer {
        val id = "${nodeId}-layer-0000"
        val name = themeService.getDefaultName(LayerType.OS)
        return OsLayer(id, name)
    }

    private fun createLayerId(node: Node): String {
        val findExisting = fun(candidate: String): String? {
            return node.layers.find{ it.id == candidate }?.id
        }
        return createLayerId(node, findExisting)
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

    fun addLayer(command: AddLayerCommand): Layer {
        val node = getById(command.nodeId)
        val layer = createLayer(command.layerType, node)
        node.layers.add(layer)
        node.ice = node.layers.any{it.type.ice}
        nodeRepo.save(node)
        return layer
    }

    private fun createLayer(layerType: LayerType, node: Node): Layer {
        val layer = node.layers.size
        val id = createLayerId(node)

        val defaultName = themeService.getDefaultName(layerType)

        return when (layerType) {
            LayerType.TEXT -> TextLayer(id, layer, defaultName)
            LayerType.ICE_PASSWORD -> IcePasswordLayer(id, layer, defaultName)
            LayerType.ICE_TANGLE -> IceTangleLayer(id, layer, defaultName)
            LayerType.TIMER_TRIGGER -> TimerTriggerLayer(id, layer, defaultName)
            LayerType.OS -> error("Cannot add OS")
        }
    }

    data class LayersUpdated(val node: Node, val layerId: String?)

    fun removeLayer(command: RemoveLayerCommand): LayersUpdated? {
        val node = getById(command.nodeId)
        val toRemove = node.layers.firstOrNull { it.id == command.layerId } ?: return null
        node.layers.remove(toRemove)
        node.layers.sortBy { it.level }
        node.layers.forEachIndexed { level, layer -> layer.level = level }
        node.ice = node.layers.any{it.type.ice}
        nodeRepo.save(node)
        val newFocusLayer = node.layers[toRemove.level - 1]
        return LayersUpdated(node, newFocusLayer.id)
    }

    fun swapLayers(command: SwapLayerCommand): LayersUpdated? {
        val node = getById(command.nodeId)
        val fromLayer = node.layers.find { it.id == command.fromId } ?: return null
        val toLayer = node.layers.find { it.id == command.toId } ?: return null

        val fromLevel = fromLayer.level
        val toLevel = toLayer.level
        fromLayer.level = toLevel
        toLayer.level = fromLevel
        node.layers.sortBy { it.level }

        nodeRepo.save(node)

        return LayersUpdated(node, null)
    }

}