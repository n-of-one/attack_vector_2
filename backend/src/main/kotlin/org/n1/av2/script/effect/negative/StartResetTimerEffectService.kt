package org.n1.av2.script.effect.negative

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.platform.util.validateDuration
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.timer.TimerEntityService
import org.n1.av2.timer.TimerLabel
import org.n1.av2.timer.TimerService
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class StartResetTimerEffectService(
    private val scriptEffectHelper: ScriptEffectHelper,
    private val timerEntityService: TimerEntityService,
    private val timerService: TimerService,
) : ScriptEffectInterface {

    override val name = "Drawback: Start reset timer"
    override val defaultValue = "00:15:00"
    override val gmDescription = "Drawback: when running this script, a countdown timer will start to reset the site. "

    override fun playerDescription(effect: ScriptEffect) = "Start a countdown of ${toHumanTime(effect.value!!)} to reset the site."

    override fun validate(effect: ScriptEffect): String? {
        if (effect.value == null) return "Duration is required."
        return effect.value.validateDuration()
    }

    override fun checkCanExecute(effect: ScriptEffect, tokens: List<String>, hackerSate: HackerState): String? {
        return scriptEffectHelper.checkHackerAtNonShutdownSite(hackerSate)
    }

    override fun execute(effect: ScriptEffect, strings: List<String>, hackerSate: HackerState): TerminalLockState {
        val siteId = hackerSate.siteId!!
        val siteTimers = timerEntityService.findByTargetSiteId(siteId)
        val hasScriptSiteShutdown = siteTimers.any { it.label == TimerLabel.SCRIPT_SITE_SHUTDOWN }
        if (hasScriptSiteShutdown) {
            // there is already a timer, do not start a second one.
            return TerminalLockState.UNLOCK
        }

        val shutdownDuration = Duration.ofMinutes(2)

        timerService.startShutdownTimer(siteId, null, effect.value!!.toDuration(), false, shutdownDuration, "Script", TimerLabel.SCRIPT_SITE_SHUTDOWN)

        return TerminalLockState.UNLOCK
    }
}
