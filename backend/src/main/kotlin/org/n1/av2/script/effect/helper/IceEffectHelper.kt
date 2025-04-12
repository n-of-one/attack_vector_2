package org.n1.av2.script.effect.helper

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.engine.SECONDS_IN_TICKS
import org.n1.av2.platform.engine.ScheduledTask
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalState
import org.n1.av2.site.entity.enums.LayerType
import org.n1.av2.statistics.IceHackState
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
) {

    fun runForSpecificIceType(
        layerType: LayerType,
        argumentTokens: List<String>,
        hackerState: HackerStateRunning,
        executionForIceLayer: (IceLayer) -> ScriptExecution
    ): ScriptExecution {
        val klass = iceService.klassFor(layerType)
        val layerDescription = iceService.formalNameFor(layerType)
        return runForIceType(klass, layerDescription, argumentTokens, hackerState, executionForIceLayer)
    }

    fun runForIceType(
        klass: KClass<out IceLayer>,
        layerDescription: String,
        argumentTokens: List<String>,
        hackerState: HackerStateRunning,
        executionForIceLayer: (IceLayer) -> ScriptExecution,
    ): ScriptExecution {
        val runOnLayerResult = scriptEffectHelper.runOnLayer(argumentTokens, hackerState)
        if (runOnLayerResult.errorExecution != null) {
            return runOnLayerResult.errorExecution
        }
        val layer = checkNotNull(runOnLayerResult.layer)
        if (layer !is IceLayer || !klass.isInstance(layer)) return ScriptExecution("This script can only be used on ${layerDescription}.")

        iceService.findOrCreateIceForLayerAndIceStatus(layer)
        return executionForIceLayer(layer)
    }

    fun autoHackSpecificIceType(layerType: LayerType, argumentTokens: List<String>, hackerState: HackerStateRunning): ScriptExecution {
        val klass = iceService.klassFor(layerType)
        val layerDescription = iceService.formalNameFor(layerType)
        return runForIceType(klass, layerDescription, argumentTokens, hackerState) { layer: IceLayer ->
            autoHack(layer, hackerState)
        }
    }

    fun autoHack(layer: IceLayer, hackerState: HackerStateRunning): ScriptExecution {
        if (layer.hacked) return ScriptExecution("This ICE has already been hacked.")
        return ScriptExecution(TerminalState.KEEP_LOCKED) {
            connectionService.replyTerminalReceiveAndLocked(true, "Hacking ICE...")
            val siteId = hackerState.siteId
            userTaskRunner.queue("start auto hack", mapOf("siteId" to siteId), Duration.ofSeconds(5)) {
                startHackedIce(layer, siteId)
            }
        }
    }

    @ScheduledTask
    private fun startHackedIce(layer: IceLayer, siteId: String) {
        val iceId = iceService.findOrCreateIceForLayerAndIceStatus(layer)
        val iceHackedAnimationSeconds = 5
        hackedUtil.iceHacked(iceId, layer.id, SECONDS_IN_TICKS * iceHackedAnimationSeconds, IceHackState.USED_SCRIPT)

        userTaskRunner.queue("complete auto hack", mapOf("siteId" to siteId), Duration.ofSeconds(iceHackedAnimationSeconds.toLong())) {
            connectionService.replyTerminalReceive("ICE hack complete.")
            connectionService.replyTerminalSetLocked(false)
        }
    }
}
