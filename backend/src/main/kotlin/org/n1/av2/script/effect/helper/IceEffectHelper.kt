package org.n1.av2.script.effect.helper

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.engine.SECONDS_IN_TICKS
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.stereotype.Service
import java.time.Duration
import kotlin.reflect.KClass

@Service
class IceEffectHelper(
    private val scriptEffectHelper: ScriptEffectHelper,
    private val connectionService: ConnectionService,
    private val hackedUtil: HackedUtil,
    private val userTaskRunner: UserTaskRunner,
    private val iceService: IceService,
    private val nodeEntityService: NodeEntityService,
    ) {

    fun runForSpecificIceLayer(
        layerType: LayerType,
        argumentTokens: List<String>,
        hackerState: HackerState,
        executionForIceLayer: (IceLayer) -> TerminalLockState
    ): ScriptExecution {
        val klass = iceService.klassFor(layerType)
        val layerDescription = iceService.nameFor(layerType)
        return runForIceLayer(klass, layerDescription, argumentTokens, hackerState, executionForIceLayer)
    }

    fun autoHackSpecificIceLayer(layerType: LayerType, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        val klass = iceService.klassFor(layerType)
        val layerDescription = iceService.nameFor(layerType)
        return runForIceLayer(klass, layerDescription, argumentTokens, hackerState) { layer: IceLayer ->
            autoHack(layer, hackerState)
            TerminalLockState.LOCK
        }
    }

    fun autoHackAnyIceLayer(argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        val klass = IceLayer::class
        val layerDescription = "ICE layers"
        return runForIceLayer(klass, layerDescription, argumentTokens, hackerState) { layer: IceLayer ->
            autoHack(layer, hackerState)
            TerminalLockState.LOCK
        }
    }

    private fun runForIceLayer(
        klass: KClass<out IceLayer>,
        layerDescription: String,
        argumentTokens: List<String>,
        hackerState: HackerState,
        executionForIceLayer: (IceLayer) -> TerminalLockState,

        ): ScriptExecution {
        scriptEffectHelper.checkInNode(hackerState)?.let { return ScriptExecution(it) }
        val layerNumber = argumentTokens.firstOrNull() ?: return ScriptExecution("Provide the [primary]layer[/] to use this script on.")
        val node = nodeEntityService.getById(hackerState.currentNodeId!!)
        val layer = node.layers.find { it.level == layerNumber.toInt() } ?: return ScriptExecution("Layer not found.")

        if (layer !is IceLayer || !klass.isInstance(layer)) return ScriptExecution("This script can only be used on ${layerDescription}.")
        if (layer.hacked) return ScriptExecution("This ICE has already been hacked.")

        return ScriptExecution {
            iceService.findOrCreateIceForLayer(layer)
            executionForIceLayer(layer)
        }
    }


    private fun autoHack(layer: IceLayer, hackerState: HackerState) {
        connectionService.replyTerminalReceive("Hacking ICE...")
        val siteId = hackerState.siteId!!
        userTaskRunner.queue("start auto hack", mapOf("siteId" to siteId), Duration.ofSeconds(5)) {
            startHackedIce(layer, siteId)
        }
    }

    private fun startHackedIce(layer: IceLayer, siteId: String) {
        val iceId = iceService.findOrCreateIceForLayer(layer)
        val iceHackedAnimationSeconds = 5
        hackedUtil.iceHacked(iceId, layer.id, SECONDS_IN_TICKS * iceHackedAnimationSeconds)

        userTaskRunner.queue("complete auto hack", mapOf("siteId" to siteId), Duration.ofSeconds(iceHackedAnimationSeconds.toLong())) {
            connectionService.replyTerminalReceive("ICE hack complete.")
            connectionService.replyTerminalSetLocked(false)
        }
    }
}
