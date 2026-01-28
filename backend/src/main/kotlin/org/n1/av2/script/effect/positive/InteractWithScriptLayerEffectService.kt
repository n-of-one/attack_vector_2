package org.n1.av2.script.effect.positive

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.layer.other.script.ScriptInteractionLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.effect.scriptCannotInteractWithThisLayer
import org.n1.av2.script.type.ScriptEffect
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.INTERACT_WITH_SCRIPT_LAYER
 */
@Service
class InteractWithScriptLayerEffectService(
    private val connectionService: ConnectionService,
    private val scriptEffectHelper: ScriptEffectHelper,
    ) : ScriptEffectInterface {

    override val name = "Trigger script layer effect"
    override val defaultValue = ""
    override val gmDescription = "Interact with a script-interaction-layer. The layer will define what happens. The text of this effect is the 'interaction key' that also needs to be set in the script-effect-layer."

    override fun playerDescription(effect: ScriptEffect) = "Unknown effect"

    override fun validate(effect: ScriptEffect) = ScriptEffectInterface.validateNonEmptyText(effect)

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerStateRunning): ScriptExecution {
        val runOnLayerResult = scriptEffectHelper.runOnLayer(argumentTokens, hackerState)
        runOnLayerResult.errorExecution?.let { return it }
        val layer = checkNotNull(runOnLayerResult.layer)

        if (layer !is ScriptInteractionLayer || !layer.interactionKey.equals(effect.value, ignoreCase = true)) {
            return ScriptExecution(scriptCannotInteractWithThisLayer)
        }

        return ScriptExecution {
            connectionService.replyTerminalReceive("Executing on layer.", "", layer.message)
        }
    }

}
