package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.layer.OsLayer
import org.n1.av2.backend.model.db.layer.TextLayer
import org.n1.av2.backend.model.db.layer.TimerTriggerLayer
import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.service.OsLayerService
import org.n1.av2.backend.service.service.ServiceIceGeneric
import org.n1.av2.backend.service.service.TextLayerService
import org.n1.av2.backend.service.service.TimerTriggerLayerService
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service

@Service
class CommandHackService(
        private val stompService: StompService,
        private val nodeService: NodeService,
        private val osLayerService: OsLayerService,
        private val textLayerService: TextLayerService,
        private val serviceIceGeneric: ServiceIceGeneric,
        private val timerTriggerLayerService: TimerTriggerLayerService,
        private val commandServiceUtil: CommandServiceUtil
) {

    fun process(runId: String, tokens: List<String>, position: HackerPosition) {
        if (tokens.size == 1) {
            return stompService.terminalReceive("Missing [primary]<layer>[/]        -- for example: [u]hack[primary] 0")
        }
        val node = nodeService.getById(position.currentNodeId)

        val level = tokens[1].toIntOrNull() ?: return reportLayerUnknown(node, tokens[1])
        if (level < 0 || level >= node.layers.size) return reportLayerUnknown(node, tokens[1])

        val blockingIceLayer = commandServiceUtil.findBlockingIceLayer(node, runId)
        if (blockingIceLayer != null && blockingIceLayer.level > level) return reportBlockingIce(blockingIceLayer)

        handleHack(node, level, position)
    }

    private fun handleHack(node: Node, level: Int, position: HackerPosition) {
        val layer = node.layers.find { it.level == level }!!

        when  {
            layer is OsLayer-> osLayerService.hack(layer, node, position)
            layer is TextLayer -> textLayerService.hack(layer, node, position.runId)
            layer is TimerTriggerLayer -> timerTriggerLayerService.hack(layer)
            layer.type.ice -> serviceIceGeneric.hack(layer, position.runId)
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

    private fun reportBlockingIce(blockingIceLayer: Layer) {
        stompService.terminalReceive("[warn b]blocked[/] - ICE (${blockingIceLayer.name}) blocks hacking. Hack the ICE first: [u]hack[/] [primary]${blockingIceLayer.level}")
    }


}