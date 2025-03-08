package org.n1.av2.script.effect.negative

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.type.ScriptEffect
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.HIDDEN_EFFECTS
 */
@Service
class HiddenEffectsService(
) : ScriptEffectInterface {

    override val name = "Drawback: other effects are hidden"
    override val defaultValue = ""
    override val gmDescription = "Drawback: the hackers will not see any effects to the right of this one. This one will show as a (*)"

    override fun playerDescription(effect: ScriptEffect) = "Unknown effect(s)"

    override fun validate(effect: ScriptEffect) = null

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState) = ScriptExecution {
        TerminalLockState.UNLOCK
    }

}
