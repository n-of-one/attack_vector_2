package org.n1.av2.script.effect.positive.ice

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.IceEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.AUTO_HACK_ANY_ICE
 */
@Service
class AutoHackAnyIceEffectService(
    private val iceEffectHelper: IceEffectHelper,
) : ScriptEffectInterface {

    override val name = "Automatically hack any ICE"
    override val defaultValue = null
    override val gmDescription = "Automatically hack any ICE."

    override fun playerDescription(effect: ScriptEffect): String {
        return "Automatically hack any layer of ICE."
    }

    override fun validate(effect: ScriptEffect) = null

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        return iceEffectHelper.runForIceType(IceLayer::class, "ICE layers", argumentTokens, hackerState) { layer: IceLayer ->
            iceEffectHelper.autoHack(layer, hackerState)
        }
    }

}
