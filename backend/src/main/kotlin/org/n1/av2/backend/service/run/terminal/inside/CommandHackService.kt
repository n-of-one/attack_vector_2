package org.n1.av2.backend.service.run.terminal.inside

import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.entity.run.HackerStateRunning
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.OsLayer
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.entity.site.layer.other.*
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.layerhacking.HackedUtil
import org.n1.av2.backend.service.layerhacking.app.StatusLightLayerService
import org.n1.av2.backend.service.layerhacking.ice.IceService
import org.n1.av2.backend.service.layerhacking.service.*
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class CommandHackService(
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val osLayerService: OsLayerService,
    private val textLayerService: TextLayerService,
    private val statusLightLayerService: StatusLightLayerService,
    private val tripwireLayerService: TripwireLayerService,
    private val commandServiceUtil: CommandServiceUtil,
    private val iceService: IceService,
    private val keystoreLayerService: KeystoreLayerService,
    private val config: ServerConfig,
    private val hackedUtil: HackedUtil,
    private val coreLayerService: CoreLayerService,
) {

    fun processHackCommand(runId: String, tokens: List<String>, state: HackerStateRunning) {
        process(runId, tokens, state, "hack", ::handleHack)
    }


    fun processQuickHack(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (!config.dev) {
            stompService.replyTerminalReceive("Unknown command, try [u]help[/].")
            return
        }
        process(runId, tokens, state, "hack", ::handleQuickHack)
    }

    fun processPasswordCommand(runId: String, tokens: List<String>, state: HackerStateRunning) {
        process(runId, tokens, state, "password ", ::handlePassword)
    }

    private fun process(runId: String, tokens: List<String>, state: HackerStateRunning, commandName: String, commandFunction: (node: Node, layer: Layer, runId: String) -> Unit) {
        if (tokens.size == 1) {
            return stompService.replyTerminalReceive("Missing [primary]<layer>[/]        -- for example: [b]${commandName}[primary] 1")
        }

        val node = nodeEntityService.getById(state.currentNodeId)

        val level = tokens[1].toIntOrNull() ?: return reportLayerUnknown(node, tokens[1])
        if (level < 0 || level >= node.layers.size) return reportLayerUnknown(node, tokens[1])

        val blockingIceLayer = commandServiceUtil.findBlockingIceLayer(node, runId)
        if (blockingIceLayer != null && blockingIceLayer.level > level) return reportBlockingIce(blockingIceLayer)

        val layer = node.layers.find { it.level == level }!!

        commandFunction(node, layer, runId)
    }


    private fun handleHack(node: Node, layer: Layer, runId: String) {
        when (layer) {
            is OsLayer -> osLayerService.hack(layer)
            is TextLayer -> textLayerService.hack(layer, node)
            is IceLayer -> hackIce(layer)
            is StatusLightLayer -> statusLightLayerService.hack(layer)
            is KeyStoreLayer -> keystoreLayerService.hack(layer)
            is CoreLayer -> coreLayerService.hack(layer, runId)
            is TripwireLayer -> tripwireLayerService.hack(layer)
            else -> stompService.replyTerminalReceive("Layer type not supported yet: ${layer.type}")
        }
    }

    private fun hackIce(layer: IceLayer) {
        if (layer.hacked) {
            stompService.replyTerminalReceive("[info]not required[/] Ice already hacked.")
            return
        }
        val iceId = iceService.findOrCreateIceForLayer(layer)

        data class EnterIce(val iceId: String)
        stompService.reply(ServerActions.SERVER_REDIRECT_HACK_ICE, EnterIce(iceId))
        stompService.replyTerminalReceive("Hack opened in new window.")
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handlePassword(node: Node, layer: Layer, runId: String) {
        if (layer !is IceLayer) {
            stompService.replyTerminalReceive("[info]not supported[/] - Only ICE can be given a password.")
            return
        }

        data class EnterIce(val layerId: String)
        stompService.reply(ServerActions.SERVER_REDIRECT_CONNECT_ICE, EnterIce(layer.id))
        stompService.replyTerminalReceive("Opened in new window.")
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleQuickHack(node: Node, layer: Layer, runId: String) {
        hackedUtil.iceHacked(layer.id, node, 0)
    }

    private fun reportLayerUnknown(node: Node, layerInput: String) {
        val layerCount = node.layers.size
        if (layerCount == 1) {
            stompService.replyTerminalReceive("[error]layer error[/] - Layer number [primary]${layerInput}[/] not found.",
                "This node has only one layer, the only valid option is: [b]hack [primary]0[/].")
        } else {
            stompService.replyTerminalReceive("[error]layer error[/] - Layer number [primary]${layerInput}[/] not found.",
                "This node has ${layerCount} layers, so use a number between [primary]0[/] and [primary]${layerCount - 1}[/].",
                "Use [b]view[/] to see the layers and their numbers.")
        }
    }

    private fun reportBlockingIce(blockingIceLayer: Layer) {
        stompService.replyTerminalReceive("[warn b]blocked[/] - ICE (${blockingIceLayer.name}) blocks hacking. Hack the ICE first: [b]hack[/] [primary]${blockingIceLayer.level}")
    }
}
