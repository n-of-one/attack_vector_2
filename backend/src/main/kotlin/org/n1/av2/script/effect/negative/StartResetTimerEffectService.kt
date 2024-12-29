package org.n1.av2.script.effect.negative

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.platform.util.validateDuration
import org.n1.av2.script.effect.ScriptEffectService
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.type.ScriptEffect
import org.springframework.stereotype.Service

@Service
class StartResetTimerEffectService(
    private val connectionService: ConnectionService,
) : ScriptEffectService {

    override val name = "Drawback: Start reset timer"
    override val defaultValue = "00:15:00"
    override val gmDescription = "Drawback: when running this script, a countdown timer will start to reset the site. "

    override fun playerDescription(effect: ScriptEffect) = "Start a countdown of ${toHumanTime(effect.value!!.toDuration())} to reset the site."

    override fun validate(effect: ScriptEffect): String? {
        if (effect.value == null) return "Duration is required."
        return effect.value.validateDuration()
    }

    override fun checkCanExecute(effect: ScriptEffect, tokens: List<String>, hackerSate: HackerState): String? {

        // FIXME check input
        return null
    }

    override fun execute(effect: ScriptEffect, strings: List<String>, state: HackerState): TerminalLockState {
        return TerminalLockState.UNLOCK
        // FIXME implement
    }
}
