package org.n1.av2.script.effect.positive.ice

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.IceEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.AUTO_HACK_ICE_TYPE
 */
@Service
class AutoHackIceTypeEffectService(
    private val iceEffectHelper: IceEffectHelper,
    private val iceService: IceService,
) : ScriptEffectInterface {

    override val name = "Automatically hack specific ICE type"
    override val defaultValue = "WORD_SEARCH_ICE"
    override val gmDescription = "Automatically hack one specific type of ICE."

    override fun playerDescription(effect: ScriptEffect): String {
        val layerType = LayerType.valueOf(effect.value!!)
        return "Automatically hack one layer of ${iceService.helpfulNameFor(layerType)}."
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
        return iceEffectHelper.autoHackSpecificIceType(iceType, argumentTokens, hackerState)
    }
}
