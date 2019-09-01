package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.db.site.enums.LayerType
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.HackerPositionService
import org.n1.av2.backend.service.service.OsLayerService
import org.n1.av2.backend.service.service.ServiceIceGeneric
import org.n1.av2.backend.service.service.TextLayerService
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service

@Service
class CommandHackService(
        private val stompService: StompService,
        private val nodeService: NodeService,
        private val hackerPositionService: HackerPositionService,
        private val osLayerService: OsLayerService,
        private val textLayerService: TextLayerService,
        private val serviceIceGeneric: ServiceIceGeneric,
        private val commandServiceUtil: CommandServiceUtil
) {

    fun process(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            return stompService.terminalReceive("Missing [primary]<layer>[/]        -- for example: [u]hack[primary] 0")

        }
        val position = hackerPositionService.retrieveForCurrentUser()
        val node = nodeService.getById(position.currentNodeId)

        val level = tokens[1].toIntOrNull() ?: return reportLayerUnknown(node, tokens[1])
        if (level < 0 || level >= node.layers.size) return reportLayerUnknown(node, tokens[1])

        val blockingIceLayer = commandServiceUtil.findBlockingIceLayer(node, runId)
        if (blockingIceLayer != null && blockingIceLayer.level > level) return reportBlockingIce(node, blockingIceLayer)

        handleHack(node, level, position)
    }

    private fun handleHack(node: Node, level: Int, position: HackerPosition) {
        val layer = node.layers.find { it.level == level }!!

        // TODO: replace with type check and remove forced coercion in underlying services.
        when (layer.type) {
            LayerType.OS -> osLayerService.hack(layer, node, position)
            LayerType.TEXT -> textLayerService.hack(layer, node, position.runId)
            LayerType.ICE_PASSWORD -> serviceIceGeneric.hack(layer, position.runId)
            else -> stompService.terminalReceive("Layer type not supported yet: ${layer.type}")
        }
    }


    private fun reportLayerUnknown(node: Node, layerInput: String) {
        val layerCount = node.layers.size
        if (layerCount == 1) {
            stompService.terminalReceive("[error]layer error[/] - Layer number [primary]${layerInput}[/] not understood.",
                    "This node has only one service, the only valid option is: [u]hack [primary]0[/].")
        } else {
            stompService.terminalReceive("[error]layer error[/] - Layer number [primary]${layerInput}[/] not understood.",
                    "This node has ${layerCount} services, so use a number between [primary]0[/] and [primary]${layerCount - 1}[/].")
        }
    }

    private fun reportBlockingIce(node: Node, blockingIceLayer: Layer) {
        stompService.terminalReceive("[warn b]blocked[/] - ICE (${blockingIceLayer.name}) blocks hacking. Hack the ICE first: [u]hack[/] [primary]${blockingIceLayer.level}")
    }


}