package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.entity.run.HackerStateRunning
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.OsLayer
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.entity.site.layer.other.TextLayer
import org.n1.av2.backend.entity.site.layer.other.TimerTriggerLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.OsLayerService
import org.n1.av2.backend.service.layerhacking.TextLayerService
import org.n1.av2.backend.service.layerhacking.TimerTriggerLayerService
import org.springframework.stereotype.Service

@Service
class CommandHackService(
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val osLayerService: OsLayerService,
    private val textLayerService: TextLayerService,
    private val snifferLayerService: TimerTriggerLayerService,
    private val commandServiceUtil: CommandServiceUtil
) {

    fun process(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (tokens.size == 1) {
            return stompService.replyTerminalReceive("Missing [primary]<layer>[/]        -- for example: [u]hack[primary] 0")
        }
        val node = nodeEntityService.getById(state.currentNodeId)

        val level = tokens[1].toIntOrNull() ?: return reportLayerUnknown(node, tokens[1])
        if (level < 0 || level >= node.layers.size) return reportLayerUnknown(node, tokens[1])

        val blockingIceLayer = commandServiceUtil.findBlockingIceLayer(node, runId)
        if (blockingIceLayer != null && blockingIceLayer.level > level) return reportBlockingIce(blockingIceLayer)

        handleHack(node, level, state)
    }

    private fun handleHack(node: Node, level: Int, state: HackerStateRunning) {
        val layer = node.layers.find { it.level == level }!!

        when  {
            layer is OsLayer -> osLayerService.hack(layer)
            layer is TextLayer -> textLayerService.hack(layer, node, state.runId)
            layer is TimerTriggerLayer -> snifferLayerService.hack(layer)
            layer is IceLayer -> hackIce(node, layer)
            else -> stompService.replyTerminalReceive("Layer type not supported yet: ${layer.type}")
        }
    }


    private fun reportLayerUnknown(node: Node, layerInput: String) {
        val layerCount = node.layers.size
        if (layerCount == 1) {
            stompService.replyTerminalReceive("[error]layer error[/] - Layer number [primary]${layerInput}[/] not understood.",
                    "This node has only one service, the only valid option is: [u]hack [primary]0[/].")
        } else {
            stompService.replyTerminalReceive("[error]layer error[/] - Layer number [primary]${layerInput}[/] not understood.",
                    "This node has ${layerCount} services, so use a number between [primary]0[/] and [primary]${layerCount - 1}[/].")
        }
    }

    private fun reportBlockingIce(blockingIceLayer: Layer) {
        stompService.replyTerminalReceive("[warn b]blocked[/] - ICE (${blockingIceLayer.name}) blocks hacking. Hack the ICE first: [u]hack[/] [primary]${blockingIceLayer.level}")
    }


    fun hackIce(node: Node, layer: IceLayer) {
        if (layer.hacked) {
            stompService.replyTerminalReceive("[info]not required[/] Ice already hacked.")
            return
        }

        data class EnterIce(val redirectId: String)
        stompService.reply(ServerActions.SERVER_REDIRECT_HACK_ICE, EnterIce("ice/${layer.id}"))
    }


}