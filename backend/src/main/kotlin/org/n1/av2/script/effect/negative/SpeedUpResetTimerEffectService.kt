package org.n1.av2.script.effect.negative

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.platform.util.validateDuration
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.timer.TimerService
import org.springframework.stereotype.Service


@Service
class SpeedUpResetTimerEffectService(
    private val scriptEffectHelper: ScriptEffectHelper,
    private val timerService: TimerService,

    ) : ScriptEffectInterface {

    override val name = "Drawback: speed up reset timer"
    override val defaultValue = "00:05:00"
    override val gmDescription = "Drawback: when running this script, an existing countdown timer " +
        "(triggered by the 'Start reset timer') effect is sped up. This prevents players from running multiple " +
        "versions of the 'Start reset timer' without any consequences. " +
        "NOTE: always set this effect higher than the effect to trigger the reset, otherwise this effect will apply when the script is first run."

    override fun playerDescription(effect: ScriptEffect) =
        "If there already was a reset countdown triggered by another script, it is sped up by ${toHumanTime(effect.value!!)}."

    override fun validate(effect: ScriptEffect): String? {
        if (effect.value == null) return "Duration is required."
        return effect.value.validateDuration()
    }

    override fun checkCanExecute(effect: ScriptEffect, tokens: List<String>, hackerSate: HackerState): String? {
        return scriptEffectHelper.checkHackerAtNonShutdownSite(hackerSate)
    }

    override fun execute(effect: ScriptEffect, strings: List<String>, state: HackerState): TerminalLockState {
        timerService.speedUpScriptResetTimer(effect.value!!.toDuration(), state.siteId!!)
        return TerminalLockState.UNLOCK
    }
}
