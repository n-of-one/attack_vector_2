package org.n1.av2.script.common

import org.n1.av2.script.effect.ScriptEffectType
import org.n1.av2.script.effect.ScriptEffectTypeLookup
import org.n1.av2.script.type.ScriptType


class UiEffectDescription(
    val description: String,
    val label: String,
)

fun ScriptType.toUiEffectDescriptions(scriptEffectTypeLookup: ScriptEffectTypeLookup): List<UiEffectDescription> {

    val hideIndex = this.effects.indexOfFirst { effect -> effect.type == ScriptEffectType.HIDDEN_EFFECTS }

    return this.effects.mapIndexed{ index, effect ->
        if (hideIndex > -1 && index > hideIndex) {
            null
        }
        else {
            val effectService = scriptEffectTypeLookup.getForType(effect.type)
            val description = effectService.playerDescription(effect)
            val label = if (effect.type == ScriptEffectType.HIDDEN_EFFECTS) "*" else index.toString()

            UiEffectDescription(description, label)
        }
    }.filterNotNull()

}
