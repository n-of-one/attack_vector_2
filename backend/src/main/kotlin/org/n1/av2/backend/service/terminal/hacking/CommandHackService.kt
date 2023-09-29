package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.entity.run.HackerStateRunning
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.OsLayer
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.entity.site.layer.other.KeyStoreLayer
import org.n1.av2.backend.entity.site.layer.other.TextLayer
import org.n1.av2.backend.entity.site.layer.other.TimerTriggerLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.KeystoreLayerService
import org.n1.av2.backend.service.layerhacking.OsLayerService
import org.n1.av2.backend.service.layerhacking.TextLayerService
import org.n1.av2.backend.service.layerhacking.TimerTriggerLayerService
import org.n1.av2.backend.service.layerhacking.ice.IceService
import org.springframework.stereotype.Service

@Service
class CommandHackService(
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val osLayerService: OsLayerService,
    private val textLayerService: TextLayerService,
    private val snifferLayerService: TimerTriggerLayerService,
    private val commandServiceUtil: CommandServiceUtil,
    private val iceService: IceService,
    private val keystoreLayerService: KeystoreLayerService,
) {

    fun processHackCommand(runId: String, tokens: List<String>, state: HackerStateRunning) {
        process(runId, tokens, state, "hack", ::handleHack)
    }

    fun processConnectCommand(runId: String, tokens: List<String>, state: HackerStateRunning) {
        process(runId, tokens, state, "connect ", ::handleConnect)
    }

    fun process(runId: String, tokens: List<String>, state: HackerStateRunning, commandName: String, commandFunction: (node: Node, layer: Layer) -> Unit) {
        if (tokens.size == 1) {
            return stompService.replyTerminalReceive("Missing [primary]<layer>[/]        -- for example: [u]${commandName}[primary] 1")
        }

        val node = nodeEntityService.getById(state.currentNodeId)

        val level = tokens[1].toIntOrNull() ?: return reportLayerUnknown(node, tokens[1])
        if (level < 0 || level >= node.layers.size) return reportLayerUnknown(node, tokens[1])

        val blockingIceLayer = commandServiceUtil.findBlockingIceLayer(node, runId)
        if (blockingIceLayer != null && blockingIceLayer.level > level) return reportBlockingIce(blockingIceLayer)

        val layer = node.layers.find { it.level == level }!!

        commandFunction(node, layer)
    }


    private fun handleHack(node: Node, layer: Layer) {
        when (layer) {
            is OsLayer -> osLayerService.hack(layer)
            is TextLayer -> textLayerService.hack(layer, node)
            is TimerTriggerLayer -> snifferLayerService.hack(layer)
            is IceLayer -> hackIce(layer)
            is KeyStoreLayer -> keystoreLayerService.hack(layer)
            else -> stompService.replyTerminalReceive("Layer type not supported yet: ${layer.type}")
        }
    }

    fun hackIce(layer: IceLayer) {
        if (layer.hacked) {
            stompService.replyTerminalReceive("[info]not required[/] Ice already hacked.")
            return
        }
        val iceId = iceService.findOrCreateIceForLayer(layer).id

        data class EnterIce(val redirectId: String)
        stompService.reply(ServerActions.SERVER_REDIRECT_HACK_ICE, EnterIce("ice/${iceId}"))
    }

    private fun handleConnect(node: Node, layer: Layer) {
        when (layer) {
            is OsLayer -> osLayerService.connect(layer)
            is TextLayer -> textLayerService.connect(layer, node)
            is TimerTriggerLayer -> snifferLayerService.connect(layer)
            is IceLayer -> connectToIce(layer)
            is KeyStoreLayer -> keystoreLayerService.connect(layer)
            else -> stompService.replyTerminalReceive("Layer type not supported yet: ${layer.type}")
        }
    }

    fun connectToIce(layer: IceLayer) {
        val iceId = iceService.findOrCreateIceForLayer(layer).id

        data class EnterIce(val redirectId: String)
        stompService.reply(ServerActions.SERVER_REDIRECT_CONNECT_ICE, EnterIce("ice/${iceId}"))
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


}
