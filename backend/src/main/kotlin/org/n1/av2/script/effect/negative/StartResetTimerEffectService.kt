package org.n1.av2.script.effect.negative

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.timer.TimerEntityService
import org.n1.av2.timer.TimerLabel
import org.n1.av2.timer.TimerService
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.START_RESET_TIMER
 */
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

    override fun validate(effect: ScriptEffect) = ScriptEffectInterface.validateDuration(effect)

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        scriptEffectHelper.checkAtNonShutdownSite(hackerState)?.let { return ScriptExecution(it) }

        return ScriptExecution {
            execute(effect, hackerState)
        }
    }

    fun execute(effect: ScriptEffect, hackerState: HackerState) {
        val siteId = hackerState.siteId!!
        val siteTimers = timerEntityService.findByTargetSiteId(siteId)
        val hasScriptSiteShutdown = siteTimers.any { it.label == TimerLabel.SCRIPT_SITE_SHUTDOWN }
        if (hasScriptSiteShutdown) {
            // there is already a timer, do not start a second one.
            return
        }

        val shutdownDuration = Duration.ofMinutes(2)

        timerService.startShutdownTimer(siteId, null, effect.value!!.toDuration(), false, shutdownDuration, "Script", TimerLabel.SCRIPT_SITE_SHUTDOWN)
    }
}
