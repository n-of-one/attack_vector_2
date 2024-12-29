package org.n1.av2.script.effect.negative

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.platform.util.validateDuration
import org.n1.av2.script.effect.ScriptEffectService
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.type.ScriptEffect
import org.springframework.stereotype.Service

@Service
class DecreaseFutureTimersEffectService(
) : ScriptEffectService {

    override val name = "Drawback: Decrease future timers"
    override val defaultValue = "00:01:00"
    override val gmDescription = "Drawback: when running this script, future timers (tripwire or otherwise) will be shorter."

    override fun playerDescription(effect: ScriptEffect) = "Decrease future timers by ${toHumanTime(effect.value!!.toDuration())}."

    override fun validate(effect: ScriptEffect): String? {
        if (effect.value == null) return "Duration is required."
        return effect.value.validateDuration()
    }

    override fun checkCanExecute(effect: ScriptEffect, tokens: List<String>, hackerSate: HackerState): String? {

        // FIXME check input
        return null
    }

    override fun execute(effect: ScriptEffect, strings: List<String>, state: HackerState): TerminalLockState {

        // FIXME implement

        return TerminalLockState.UNLOCK
    }
}
