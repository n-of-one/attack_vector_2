package org.n1.av2.backend.entity.site

import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.OsLayer
import org.n1.av2.backend.entity.site.layer.ice.*
import org.n1.av2.backend.entity.site.layer.other.KeyStoreLayer
import org.n1.av2.backend.entity.site.layer.other.StatusLightLayer
import org.n1.av2.backend.entity.site.layer.other.TextLayer
import org.n1.av2.backend.entity.site.layer.other.TimerTriggerLayer
import org.n1.av2.backend.model.ui.*
import org.n1.av2.backend.service.ThemeService
import org.n1.av2.backend.service.layerhacking.app.status_light.StatusLightEntityService
import org.n1.av2.backend.util.createId
import org.n1.av2.backend.util.createLayerId
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

const val NODE_MIN_X = 35
const val NODE_MAX_X = 607 - 35

const val NODE_MIN_Y = 35
const val NODE_MAX_Y = 815 - 48 - 100


@Service
class NodeEntityService(
    private val nodeRepo: NodeRepo,
    private val themeService: ThemeService,
    private val statusLightEntityService: StatusLightEntityService,
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
                hacked = false,
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

    fun findById(nodeId: String): Node {
        return nodeRepo.findById(nodeId).getOrElse { error("Node not found for id: ${nodeId}") }
    }

    fun findByNetworkId(siteId: String, networkId: String): Node? {
        return nodeRepo.findBySiteIdAndNetworkId(siteId, networkId) ?: nodeRepo.findBySiteIdAndNetworkIdIgnoreCase(siteId, networkId)
    }

    fun findByLayerId(layerId: String): Node {
        val layerIdParts= layerId.split(":")
        if (layerIdParts.size != 2) error("Invalid layer id: ${layerId}")

        return findById(layerIdParts[0])
    }

    fun findLayer(layerId: String): Layer {
        val node = findByLayerId(layerId)
        return node.getLayerById(layerId)
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
        val level = node.layers.size
        val layerId = createLayerId(node)

        val defaultName = themeService.getDefaultName(layerType)

        return when (layerType) {
            LayerType.TEXT -> TextLayer(layerId, level, defaultName)
            LayerType.PASSWORD_ICE -> PasswordIceLayer(layerId, level, defaultName)
            LayerType.TANGLE_ICE -> TangleIceLayer(layerId, level, defaultName)
            LayerType.WORD_SEARCH_ICE -> WordSearchIceLayer(layerId, level, defaultName)
            LayerType.NETWALK_ICE -> NetwalkIceLayer(layerId, level, defaultName)
            LayerType.TAR_ICE -> TarIceLayer(layerId, level, defaultName)
            LayerType.OS -> error("Cannot add OS")
            LayerType.TIMER_TRIGGER -> TimerTriggerLayer(layerId, level, defaultName)
            LayerType.STATUS_LIGHT -> createStatusLightLayer(layerId, LayerType.STATUS_LIGHT, level, defaultName, "off", "on")
            LayerType.LOCK -> createStatusLightLayer(layerId,LayerType.LOCK, level, defaultName,  "locked", "unlocked")
            LayerType.KEYSTORE -> KeyStoreLayer(layerId, level, defaultName)
        }
    }

    private fun createStatusLightLayer(layerId: String, type: LayerType, level: Int, defaultName: String, textForRed: String, textForGreen: String): StatusLightLayer {
        val entity = statusLightEntityService.create(layerId, false, defaultName, textForRed, textForGreen)
        return  StatusLightLayer(layerId, type, level, defaultName, entity.id, textForRed, textForGreen)
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

    fun deleteAllForSite(siteId: String) {
        nodeRepo.findBySiteId(siteId).forEach { nodeRepo.delete(it) }
    }

}