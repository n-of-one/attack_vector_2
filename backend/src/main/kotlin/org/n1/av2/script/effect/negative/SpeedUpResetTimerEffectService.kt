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
class SpeedUpResetTimerEffectService(
    private val connectionService: ConnectionService,
) : ScriptEffectService {

    override val name = "Drawback: Start reset timer"
    override val defaultValue = "00:15:00"
    override val gmDescription = "Drawback: when running this script, an existing countdown timer " +
        "(triggered by the 'Start reset timer') effect is sped up.<br/>\n This prevents players from running multiple " +
        "versions of the 'Start reset timer' without any consequences."

    override fun playerDescription(effect: ScriptEffect) =
        "If there already was a reset countdown triggered by another script, it is sped up by ${toHumanTime(effect.value!!.toDuration())}."

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
