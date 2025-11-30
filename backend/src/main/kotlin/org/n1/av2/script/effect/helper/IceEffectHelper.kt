package org.n1.av2.script.effect.helper

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
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
    private val configService: ConfigService,
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
            val quickPlaying = configService.getAsBoolean(ConfigItem.DEV_QUICK_PLAYING)

            val duration = if (quickPlaying) Duration.ofMillis(50) else Duration.ofSeconds(5)

            userTaskRunner.queue("complete auto hack", mapOf("siteId" to siteId), duration) {
                iceHackedComplete(layer)
            }
        }
    }

    @ScheduledTask
    private fun iceHackedComplete(layer: IceLayer) {
        val iceId = iceService.findOrCreateIceForLayerAndIceStatus(layer)
        hackedUtil.iceHacked(iceId, layer.id, 0, IceHackState.USED_SCRIPT)
        connectionService.replyTerminalReceive("ICE hack complete.")
    }
}
