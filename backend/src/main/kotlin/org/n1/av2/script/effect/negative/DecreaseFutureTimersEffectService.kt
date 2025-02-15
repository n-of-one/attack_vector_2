package org.n1.av2.script.effect.negative

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.platform.util.validateDuration
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service
import java.time.Duration

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

    override fun validate(effect: ScriptEffect): String? {
        if (effect.value == null) return "Duration is required."
        return effect.value.validateDuration()
    }

    override fun checkCanExecute(effect: ScriptEffect, tokens: List<String>, hackerSate: HackerState): String? {
        return scriptEffectHelper.checkHackerAtNonShutdownSite(hackerSate)
    }

    override fun execute(effect: ScriptEffect, strings: List<String>, hackerSate: HackerState): TerminalLockState {
        val siteProperties = sitePropertiesEntityService.getBySiteId(hackerSate.siteId!!)

        val scriptDurationChange = effect.value!!.toDuration()
        val currentAlertnessAdjustment:Duration = siteProperties.alertnessTimerAdjustment ?: Duration.ZERO
        val newAdjustment = currentAlertnessAdjustment - scriptDurationChange

        sitePropertiesEntityService.save(
            siteProperties.copy(
                alertnessTimerAdjustment = newAdjustment
            )
        )
        connectionService.replyTerminalReceive("System alertness increased,  future timers will be decreased by ${toHumanTime(effect.value)}.")

        return TerminalLockState.UNLOCK
    }
}
