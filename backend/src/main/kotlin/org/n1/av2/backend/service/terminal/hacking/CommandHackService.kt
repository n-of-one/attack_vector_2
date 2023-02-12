package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.layer.OsLayer
import org.n1.av2.backend.model.db.layer.TextLayer
import org.n1.av2.backend.model.db.layer.TimerTriggerLayer
import org.n1.av2.backend.model.db.run.HackerStateRunning
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layer.OsLayerService
import org.n1.av2.backend.service.layer.ServiceIceGeneric
import org.n1.av2.backend.service.layer.TextLayerService
import org.n1.av2.backend.service.layer.TimerTriggerLayerService
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service

@Service
class CommandHackService(
        private val stompService: StompService,
        private val nodeService: NodeService,
        private val osLayerService: OsLayerService,
        private val textLayerService: TextLayerService,
        private val serviceIceGeneric: ServiceIceGeneric,
        private val snifferLayerService: TimerTriggerLayerService,
        private val commandServiceUtil: CommandServiceUtil
) {

    fun process(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (tokens.size == 1) {
            return stompService.terminalReceiveCurrentUser("Missing [primary]<layer>[/]        -- for example: [u]hack[primary] 0")
        }
        val node = nodeService.getById(state.currentNodeId)

        val level = tokens[1].toIntOrNull() ?: return reportLayerUnknown(node, tokens[1])
        if (level < 0 || level >= node.layers.size) return reportLayerUnknown(node, tokens[1])

        val blockingIceLayer = commandServiceUtil.findBlockingIceLayer(node, runId)
        if (blockingIceLayer != null && blockingIceLayer.level > level) return reportBlockingIce(blockingIceLayer)

        handleHack(node, level, state)
    }

    private fun handleHack(node: Node, level: Int, state: HackerStateRunning) {
        val layer = node.layers.find { it.level == level }!!

        when  {
            layer is OsLayer-> osLayerService.hack(layer)
            layer is TextLayer -> textLayerService.hack(layer, node, state.runId)
            layer is TimerTriggerLayer -> snifferLayerService.hack(layer)
            layer.type.ice -> serviceIceGeneric.hack(layer, state.runId)
            else -> stompService.terminalReceiveCurrentUser("Layer type not supported yet: ${layer.type}")
        }
    }


    private fun reportLayerUnknown(node: Node, layerInput: String) {
        val layerCount = node.layers.size
        if (layerCount == 1) {
            stompService.terminalReceiveCurrentUser("[error]layer error[/] - Layer number [primary]${layerInput}[/] not understood.",
                    "This node has only one service, the only valid option is: [u]hack [primary]0[/].")
        } else {
            stompService.terminalReceiveCurrentUser("[error]layer error[/] - Layer number [primary]${layerInput}[/] not understood.",
                    "This node has ${layerCount} services, so use a number between [primary]0[/] and [primary]${layerCount - 1}[/].")
        }
    }

    private fun reportBlockingIce(blockingIceLayer: Layer) {
        stompService.terminalReceiveCurrentUser("[warn b]blocked[/] - ICE (${blockingIceLayer.name}) blocks hacking. Hack the ICE first: [u]hack[/] [primary]${blockingIceLayer.level}")
    }


}