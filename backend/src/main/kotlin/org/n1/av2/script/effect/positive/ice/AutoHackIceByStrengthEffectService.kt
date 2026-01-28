package org.n1.av2.script.effect.positive.ice

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.platform.inputvalidation.ValidationException
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.IceEffectHelper
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.effect.scriptCannotInteractWithThisLayer
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.ThemeService
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.AUTO_HACK_ICE_BY_STRENGTH
 */
@Service
class AutoHackIceByStrengthEffectService(
    private val iceEffectHelper: IceEffectHelper,
    private val scriptEffectHelper: ScriptEffectHelper,
    private val themeService: ThemeService,

    ) : ScriptEffectInterface {

    override val name = "Automatically hack ICE by strength"
    override val defaultValue = "${IceStrength.WEAK.name}:${LayerType.PASSWORD_ICE},${LayerType.TAR_ICE}"
    override val gmDescription = "Automatically hack ICE with low enough strength."

    override fun playerDescription(effect: ScriptEffect): String {
        val strength = parseStrength(effect)
        val excludedIceTypes = parseExcludedIceTypes(effect)

        val orWeakerText = if (strength.value > IceStrength.VERY_WEAK.value) " or below" else ""

        return "Automatically hack ICE with strength '${strength.description.lowercase()}'${orWeakerText}. Excluded ICE types: ${excludedIceTypes.joinToString(", ") { themeService.themeName(it) }}."
    }

    private fun parseStrength(effect: ScriptEffect): IceStrength {
        val strength = effect.value?.split(":")[0] ?: throw ValidationException("Invalid ICE strength")
        return IceStrength.valueOf(strength)
    }

    private fun parseExcludedIceTypes(effect: ScriptEffect): List<LayerType> {
        val excludeIceTypes = effect.value?.split(":")[1] ?:  throw ValidationException("Invalid exclusion string")
        return excludeIceTypes.split(",").map {LayerType.valueOf(it)}
    }

    override fun validate(effect: ScriptEffect): String? {
        try {
            parseStrength(effect)
            parseExcludedIceTypes(effect)
            return null
        }
        catch (_: Exception) {
            return "Invalid effect value format. Expected format: ICE_STRENGTH:EXCLUDED_ICE_TYPES. Example: WEAK:PASSWORD_ICE,TAR_ICE"
        }
    }

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerStateRunning): ScriptExecution {
        val runOnLayerResult = scriptEffectHelper.runOnLayer(argumentTokens, hackerState)
        runOnLayerResult.errorExecution?.let { return it }

        val layer = checkNotNull(runOnLayerResult.layer)
        if (layer !is IceLayer ) {
            return ScriptExecution(scriptCannotInteractWithThisLayer)
        }
        val excludedIceTypes = parseExcludedIceTypes(effect)
        if (excludedIceTypes.contains(layer.type)) {
            return ScriptExecution("Script cannot hack this type of ICE.")
        }
        val scriptStrength = parseStrength(effect)
        if (scriptStrength.value < layer.strength.value) {
            return ScriptExecution("Script is too weak to automatically hack ICE of strength: ${layer.strength.description}.")
        }

        return iceEffectHelper.autoHack(layer, hackerState)
    }
}
