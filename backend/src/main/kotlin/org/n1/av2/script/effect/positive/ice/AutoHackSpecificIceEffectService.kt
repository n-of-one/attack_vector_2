package org.n1.av2.script.effect.positive.ice

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.engine.TICK_MILLIS
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.AUTO_HACK_SPECIFIC_ICE
 */
@Service
class AutoHackSpecificIceEffectService(
    private val scriptEffectHelper: ScriptEffectHelper,
    private val connectionService: ConnectionService,
    private val hackedUtil: HackedUtil,
    private val userTaskRunner: UserTaskRunner,
    private val iceService: IceService,
) : ScriptEffectInterface {

    override val name = "Automatically hack ICE"
    override val defaultValue = "WORD_SEARCH_ICE"
    override val gmDescription = "Automatically hack one layer of ICE."

    override fun playerDescription(effect: ScriptEffect): String {
        val layerType = LayerType.valueOf(effect.value!!)
        return "Automatically hack one layer of ${iceService.nameFor(layerType)}."
    }

    override fun validate(effect: ScriptEffect): String? {
        if (effect.value == null) return "ICE type is required."
        try {
            LayerType.valueOf(effect.value)
            return null
        }
        catch (_: IllegalArgumentException) {
            return "Invalid ICE type."
        }
    }

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        val iceType = LayerType.valueOf(effect.value!!)

        return scriptEffectHelper.runForIceLayer(iceType, argumentTokens, hackerState) { layer: IceLayer ->
            connectionService.replyTerminalReceive("Hacking ICE...")
            val siteId = hackerState.siteId!!
            userTaskRunner.queue("start auto hack", mapOf("siteId" to siteId), Duration.ofSeconds(5)) {
                startHackedIce(layer, siteId)
            }
            TerminalLockState.LOCK
        }
    }


    private fun startHackedIce(layer: IceLayer, siteId: String) {
        val iceId = iceService.findOrCreateIceForLayer(layer)
        hackedUtil.iceHacked(iceId, layer.id, (1000 / TICK_MILLIS) * 5) // Allow the animation to finish

        userTaskRunner.queue("complete auto hack", mapOf("siteId" to siteId), Duration.ofSeconds(5)) {
            connectionService.replyTerminalReceive("ICE hack complete.")
            connectionService.replyTerminalSetLocked(false)
        }
    }

}
