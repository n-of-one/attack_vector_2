package org.n1.av2.script.common

import org.n1.av2.platform.iam.user.UserAndHackerService
import org.n1.av2.script.effect.ScriptEffectLookup
import org.n1.av2.script.type.ScriptEffectType
import org.n1.av2.script.type.ScriptType


class UiEffectDescription(
    val description: String,
    val label: String,
)

fun ScriptType.toUiEffectDescriptions(scriptEffectLookup: ScriptEffectLookup): List<UiEffectDescription> {

    val hideIndex = this.effects.indexOfFirst { effect -> effect.type == ScriptEffectType.HIDDEN_EFFECTS }

    return this.effects.mapIndexed{ index, effect ->
        if (hideIndex > -1 && index > hideIndex) {
            null
        }
        else {
            val effectService = scriptEffectLookup.getForType(effect.type)
            val description = effectService.playerDescription(effect)
            val label = if (effect.type == ScriptEffectType.HIDDEN_EFFECTS) "*" else index.toString()

            UiEffectDescription(description, label)
        }
    }.filterNotNull()

}
