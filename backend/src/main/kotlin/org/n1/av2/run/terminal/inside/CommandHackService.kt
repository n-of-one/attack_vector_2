package org.n1.av2.run.terminal.inside

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.layer.Layer
import org.n1.av2.layer.app.status_light.StatusLightLayer
import org.n1.av2.layer.app.status_light.StatusLightLayerService
import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.layer.other.core.CoreLayerService
import org.n1.av2.layer.other.keystore.KeyStoreLayer
import org.n1.av2.layer.other.keystore.KeystoreLayerService
import org.n1.av2.layer.other.os.OsLayer
import org.n1.av2.layer.other.os.OsLayerService
import org.n1.av2.layer.other.text.TextLayer
import org.n1.av2.layer.other.text.TextLayerService
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.layer.other.tripwire.TripwireLayerService
import org.n1.av2.platform.config.ServerConfig
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class CommandHackService(
    private val connectionService: ConnectionService,
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
            connectionService.replyTerminalReceive("Unknown command, try [u]help[/].")
            return
        }
        process(runId, tokens, state, "hack", ::handleQuickHack)
    }

    fun processPasswordCommand(runId: String, tokens: List<String>, state: HackerStateRunning) {
        process(runId, tokens, state, "password ", ::handlePassword)
    }

    private fun process(runId: String, tokens: List<String>, state: HackerStateRunning, commandName: String, commandFunction: (node: Node, layer: Layer, runId: String) -> Unit) {
        if (tokens.size == 1) {
            return connectionService.replyTerminalReceive("Missing [primary]<layer>[/]        -- for example: [b]${commandName}[primary] 1")
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
            else -> connectionService.replyTerminalReceive("Layer type not supported yet: ${layer.type}")
        }
    }

    private fun hackIce(layer: IceLayer) {
        if (layer.hacked) {
            connectionService.replyTerminalReceive("[info]not required[/] Ice already hacked.")
            return
        }
        val iceId = iceService.findOrCreateIceForLayer(layer)

        data class EnterIce(val iceId: String)
        connectionService.reply(ServerActions.SERVER_REDIRECT_HACK_ICE, EnterIce(iceId))
        connectionService.replyTerminalReceive("Hack opened in new window.")
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handlePassword(node: Node, layer: Layer, runId: String) {
        if (layer !is IceLayer) {
            connectionService.replyTerminalReceive("[info]not supported[/] - Only ICE can be given a password.")
            return
        }

        data class EnterIce(val layerId: String)
        connectionService.reply(ServerActions.SERVER_REDIRECT_CONNECT_ICE, EnterIce(layer.id))
        connectionService.replyTerminalReceive("Opened in new window.")
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleQuickHack(node: Node, layer: Layer, runId: String) {
        hackedUtil.iceHacked(layer.id, node, 0)
        connectionService.replyTerminalReceive("Quick hacked ${layer.level}.")
    }

    private fun reportLayerUnknown(node: Node, layerInput: String) {
        val layerCount = node.layers.size
        if (layerCount == 1) {
            connectionService.replyTerminalReceive("[error]layer error[/] - Layer number [primary]${layerInput}[/] not found.",
                "This node has only one layer, the only valid option is: [b]hack [primary]0[/].")
        } else {
            connectionService.replyTerminalReceive("[error]layer error[/] - Layer number [primary]${layerInput}[/] not found.",
                "This node has ${layerCount} layers, so use a number between [primary]0[/] and [primary]${layerCount - 1}[/].",
                "Use [b]view[/] to see the layers and their numbers.")
        }
    }

    private fun reportBlockingIce(blockingIceLayer: Layer) {
        connectionService.replyTerminalReceive("[warn b]blocked[/] - ICE (${blockingIceLayer.name}) blocks hacking. Hack the ICE first: [b]hack[/] [primary]${blockingIceLayer.level}")
    }
}
