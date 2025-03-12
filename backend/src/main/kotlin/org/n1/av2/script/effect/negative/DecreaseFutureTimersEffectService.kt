package org.n1.av2.script.effect.negative

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.DECREASE_FUTURE_TIMERS
 */
@Service
class DecreaseFutureTimersEffectService(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val connectionService: ConnectionService,
    private val scriptEffectHelper: ScriptEffectHelper,
) : ScriptEffectInterface {

    override val name = "Drawback: Decrease future timers"
    override val defaultValue = "00:01:00"
    override val gmDescription = "Drawback: when running this script, future timers (tripwire or otherwise) will be shorter."

    override fun playerDescription(effect: ScriptEffect) = "Decrease future timers by ${toHumanTime(effect.value!!)}."

    override fun validate(effect: ScriptEffect) = ScriptEffectInterface.validateDuration(effect)

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        scriptEffectHelper.checkAtNonShutdownSite(hackerState)?.let { return ScriptExecution(it) }

        return ScriptExecution {
            val siteProperties = sitePropertiesEntityService.getBySiteId(hackerState.siteId!!)

            val scriptDurationChange = effect.value!!.toDuration()
            val currentAlertnessAdjustment: Duration = siteProperties.alertnessTimerAdjustment ?: Duration.ZERO
            val newAdjustment = currentAlertnessAdjustment - scriptDurationChange

            sitePropertiesEntityService.save(
                siteProperties.copy(
                    alertnessTimerAdjustment = newAdjustment
                )
            )
            connectionService.replyTerminalReceive("System alertness increased,  future timers will be decreased by ${toHumanTime(effect.value)}.")
        }
    }
}
