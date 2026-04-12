package org.n1.av2.site.entity

import org.n1.av2.editor.*
import org.n1.av2.layer.Layer
import org.n1.av2.layer.app.status_light.StatusLightLayer
import org.n1.av2.layer.ice.netwalk.NetwalkIceLayer
import org.n1.av2.layer.ice.password.PasswordIceLayer
import org.n1.av2.layer.ice.sweeper.SweeperIceLayer
import org.n1.av2.layer.ice.tangle.TangleIceLayer
import org.n1.av2.layer.ice.tar.TarIceLayer
import org.n1.av2.layer.ice.wordsearch.WordSearchIceLayer
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.layer.other.keystore.KeyStoreLayer
import org.n1.av2.layer.other.os.OsLayer
import org.n1.av2.layer.other.script.ScriptCreditsLayer
import org.n1.av2.layer.other.script.ScriptInteractionLayer
import org.n1.av2.layer.other.text.TextLayer
import org.n1.av2.layer.other.timeradjuster.TimerAdjusterLayer
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.platform.util.createId
import org.n1.av2.platform.util.createLayerId
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

const val CANVAS_WIDTH = 1250
const val CANVAS_HEIGHT = 865

const val MARGIN = 10
const val MARGIN_BOTTOM_FOR_PLAYER = 113

const val NODE_MIN_X = MARGIN
const val NODE_MAX_X = CANVAS_WIDTH - MARGIN

const val NODE_MIN_Y = MARGIN
const val NODE_MAX_Y = CANVAS_HEIGHT - MARGIN - MARGIN_BOTTOM_FOR_PLAYER


@Service
class NodeEntityService(
    private val nodeRepo: NodeRepo,
    private val themeService: ThemeService,
) {

    fun createNode(command: AddNode): Node {
        val id = createId("node", nodeRepo::findById)
        val siteId = command.siteId
        val nodes = findBySiteId(siteId)
        val networkId = command.networkId ?: nextFreeNetworkId(siteId, nodes)
        val layers = mutableListOf(createOsLayer(id))

        val x = capX(command.x)
        val y = capY(command.y)

        val node = Node(
            id = id,
            siteId = siteId,
            type = command.type,
            x = x,
            y = y,
            layers = layers,
            networkId = networkId
        )
        nodeRepo.save(node)
        return node
    }

    private fun createOsLayer(nodeId: String): Layer {
        val name = themeService.themeName(LayerType.OS)
        val layerId = createOsLayerId(nodeId)
        return OsLayer(layerId, name)
    }

    fun createOsLayerId(nodeId: String): String {
        return "${nodeId}:layer-0000"
    }

    fun createLayerId(node: Node): String {
        val findExisting = fun(candidate: String): String? {
            return node.layers.find { it.id == candidate }?.id
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

    fun findBySiteId(siteId: String): List<Node> {
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

    fun snap(siteId: String) {
        val nodes = findBySiteId(siteId)
        nodes.forEach { node ->

            val xOffset = (CANVAS_WIDTH / 2) % 40
            val yOffset = (CANVAS_HEIGHT / 2) % 40

            val x = capX(40 * ((node.x - xOffset + 20) / 40) + xOffset)
            val y = capY(40 * ((node.y - yOffset + 20) / 40) + yOffset)
            val copy = node.copy(x = x, y = y)

            nodeRepo.save(copy)
        }
    }

    fun center(siteId: String, startNodeNetworkId: String) {
        val nodes = findBySiteId(siteId)

        val startNode = nodes.find { it.networkId == startNodeNetworkId } ?: error("Start node not found: ${startNodeNetworkId}")

        val center = CANVAS_WIDTH / 2
        val dx = center - startNode.x

        nodes.forEach { node ->
            val newX = node.x + dx
            val cappedNewX = capX(newX)
            if (newX != cappedNewX) {
                error("Cannot center as that would move node: ${node.networkId} outside of the map")
            }
        }

        nodes.forEach { node ->
            val newX = node.x + dx
            nodeRepo.save(node.copy(x = newX))
        }

        nodeRepo.save(startNode.copy(x = center))
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
        val nodeId = deriveNodeIdFromLayerId(layerId)
        return findById(nodeId)
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
        nodeRepo.save(node)
        return layer
    }

    private fun createLayer(layerType: LayerType, node: Node): Layer {
        val level = node.layers.size
        val layerId = createLayerId(node)

        val defaultName = themeService.themeName(layerType)

        return when (layerType) {
            LayerType.TEXT -> TextLayer(layerId, level, defaultName)
            LayerType.PASSWORD_ICE -> PasswordIceLayer(layerId, level, defaultName)
            LayerType.TANGLE_ICE -> TangleIceLayer(layerId, level, defaultName)
            LayerType.WORD_SEARCH_ICE -> WordSearchIceLayer(layerId, level, defaultName)
            LayerType.NETWALK_ICE -> NetwalkIceLayer(layerId, level, defaultName)
            LayerType.TAR_ICE -> TarIceLayer(layerId, level, defaultName)
            LayerType.SWEEPER_ICE -> SweeperIceLayer(layerId, level, defaultName)
            LayerType.OS -> error("Cannot add OS")
            LayerType.STATUS_LIGHT -> createStatusLightLayer(layerId, LayerType.STATUS_LIGHT, level, defaultName, "Status", "off", "on")
            LayerType.LOCK -> createStatusLightLayer(layerId, LayerType.LOCK, level, defaultName, "Lock status", "locked", "unlocked")
            LayerType.KEYSTORE -> KeyStoreLayer(layerId, level, defaultName)
            LayerType.TRIPWIRE -> TripwireLayer(layerId, level, defaultName)
            LayerType.TIMER_ADJUSTER -> TimerAdjusterLayer(layerId, level, defaultName)
            LayerType.CORE -> CoreLayer(layerId, level, defaultName)
            LayerType.SCRIPT_INTERACTION -> ScriptInteractionLayer(layerId, level, defaultName)
            LayerType.SCRIPT_CREDITS -> ScriptCreditsLayer(layerId, level, defaultName)
        }
    }

    private fun createStatusLightLayer(
        layerId: String,
        type: LayerType,
        level: Int,
        defaultName: String,
        switchLabel: String,
        textForOption1: String,
        textForOption2: String
    ): StatusLightLayer {
        return StatusLightLayer(layerId, type, level, defaultName, switchLabel, textForOption1, textForOption2)
    }

    data class LayersUpdated(val node: Node, val layerId: String?)

    fun removeLayer(command: RemoveLayerCommand): LayersUpdated? {
        val node = getById(command.nodeId)
        val toRemove = node.layers.firstOrNull { it.id == command.layerId } ?: return null
        node.layers.remove(toRemove)
        node.layers.sortBy { it.level }
        node.layers.forEachIndexed { level, layer -> layer.level = level }
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

    fun findAllCores(): Map<Node, List<CoreLayer>> {
        return nodeRepo.findAll()
            .filter {
                it.layers.any { it.type == LayerType.CORE }
            }.associate { node ->
                val coreLayers = node.layers.filterIsInstance<CoreLayer>()
                node to coreLayers
            }
    }

    fun findAllCores(siteId: String): List<CoreLayer> {
        return nodeRepo.findBySiteId(siteId)
            .filter {
                it.layers.any { it.type == LayerType.CORE }
            }.map { node ->
                node.layers.filterIsInstance<CoreLayer>()
            }.flatten()
    }


    fun findAllTripwiresWithCore(coreLayerId: String): List<Node> {
        return nodeRepo.findAll()
            .filter { node ->
                val tripwireLayers =
                    node.layers
                        .filterIsInstance<TripwireLayer>()
                        .filter { tripwireLayer: TripwireLayer -> tripwireLayer.coreLayerId == coreLayerId }
                tripwireLayers.isNotEmpty()
            }
    }

    companion object {
        fun deriveNodeIdFromLayerId(layerId: String): String {
            val layerIdParts = layerId.split(":")
            if (layerIdParts.size != 2) error("Invalid layer id: ${layerId}")
            return layerIdParts[0]
        }
    }
}
