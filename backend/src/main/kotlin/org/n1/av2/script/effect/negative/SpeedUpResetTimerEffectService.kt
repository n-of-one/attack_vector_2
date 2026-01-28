package org.n1.av2.script.effect.negative

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.timer.TimerService
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.SPEED_UP_RESET_TIMER
 */
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

    override fun validate(effect: ScriptEffect) = ScriptEffectInterface.validateDuration(effect)

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerStateRunning): ScriptExecution {
        scriptEffectHelper.checkAtNonShutdownSite(hackerState)?.let { return ScriptExecution(it) }

        return ScriptExecution {
            timerService.speedUpScriptResetTimer(effect.value!!.toDuration(), hackerState.siteId)
        }
    }
}
