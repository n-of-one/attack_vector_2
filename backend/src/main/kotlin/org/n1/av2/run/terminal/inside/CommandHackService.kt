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
import org.n1.av2.layer.other.script.ScriptInteractionLayer
import org.n1.av2.layer.other.script.ScriptInteractionLayerService
import org.n1.av2.layer.other.text.TextLayer
import org.n1.av2.layer.other.text.TextLayerService
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.layer.other.tripwire.TripwireLayerService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.run.terminal.generic.DevCommandHelper
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.statistics.IceHackState
import org.springframework.stereotype.Service

@Service
class CommandHackService(
    private val connectionService: ConnectionService,
    private val nodeEntityService: NodeEntityService,
    private val osLayerService: OsLayerService,
    private val textLayerService: TextLayerService,
    private val statusLightLayerService: StatusLightLayerService,
    private val tripwireLayerService: TripwireLayerService,
    private val iceService: IceService,
    private val keystoreLayerService: KeystoreLayerService,
    private val hackedUtil: HackedUtil,
    private val coreLayerService: CoreLayerService,
    private val scriptInteractionLayerService: ScriptInteractionLayerService,
    private val insideTerminalHelper: InsideTerminalHelper,
    private val devCommandHelper: DevCommandHelper,
) {

    fun processHackCommand(arguments: List<String>, hackerState: HackerStateRunning) {
        process(arguments, hackerState, "hack", ::handleHack)
    }

    fun processQuickHack(arguments: List<String>, hackerState: HackerStateRunning) {
        if (!devCommandHelper.checkDevModeEnabled()) return
        process(arguments, hackerState, "hack", ::handleQuickHack)
    }

    fun processPasswordCommand(arguments: List<String>, hackerState: HackerStateRunning) {
        process(arguments, hackerState, "password ", ::handlePassword)
    }

    private fun process(arguments: List<String>, hackerState: HackerStateRunning, commandName: String, commandFunction: (layer: Layer, hackerState: HackerStateRunning) -> Unit) {
        if (!insideTerminalHelper.verifyInside(hackerState)) return
        if (arguments.isEmpty()) {
            connectionService.replyTerminalReceive("Missing [primary]<layer>[/]      -- for example: [b]${commandName}[primary] 1")
            return
        }
        val layer = insideTerminalHelper.verifyCanAccessLayer(arguments.first(), hackerState, commandName) ?: return

        commandFunction(layer, hackerState)
    }


    @Suppress("unused")
    fun handleHack(layer: Layer, hackerState: HackerStateRunning) {
        when (layer) {
            is OsLayer -> osLayerService.hack(layer)
            is TextLayer -> textLayerService.hack(layer)
            is IceLayer -> hackIce(layer)
            is StatusLightLayer -> statusLightLayerService.hack(layer)
            is KeyStoreLayer -> keystoreLayerService.hack(layer)
            is CoreLayer -> coreLayerService.hack(layer, hackerState.runId)
            is TripwireLayer -> tripwireLayerService.hack(layer, hackerState)
            is ScriptInteractionLayer -> scriptInteractionLayerService.hack()
            else -> connectionService.replyTerminalReceive("Layer type not supported yet: ${layer.type} ${layer.javaClass.name}").also { error("Non implemented layer type: ${layer.type}") }
        }
    }

    private fun hackIce(layer: IceLayer) {
        if (layer.hacked) {
            connectionService.replyTerminalReceive("[info]not required[/] Ice already hacked.")
            return
        }
        val iceId = iceService.findOrCreateIceForLayerAndIceStatus(layer)

        data class EnterIce(val iceId: String)
        connectionService.reply(ServerActions.SERVER_REDIRECT_HACK_ICE, EnterIce(iceId))
        connectionService.replyTerminalReceive("Hack opened in new window.")
    }

    @Suppress("unused")
    private fun handlePassword(layer: Layer, hackerState: HackerStateRunning) {
        if (layer !is IceLayer) {
            connectionService.replyTerminalReceive("[info]not supported[/] - Only ICE can be given a password.")
            return
        }

        data class EnterIce(val layerId: String)
        connectionService.reply(ServerActions.SERVER_REDIRECT_CONNECT_ICE, EnterIce(layer.id))
        connectionService.replyTerminalReceive("Opened in new window.")
    }

    @Suppress("unused")
    private fun handleQuickHack(layer: Layer, hackerState: HackerStateRunning) {
        if (layer !is IceLayer ) {
            connectionService.replyTerminalReceive("[info]not supported[/] - Only ICE can be quick hacked.")
            return
        }
        val iceId = iceService.findOrCreateIceForLayerAndIceStatus(layer)

        requireNotNull(hackerState.currentNodeId)
        val node = nodeEntityService.getById(hackerState.currentNodeId)

        hackedUtil.iceHacked(iceId, layer.id, node, 0, IceHackState.USED_DEV_MODE)
        connectionService.replyTerminalReceive("Quick hacked ${layer.level}.")
    }


}
