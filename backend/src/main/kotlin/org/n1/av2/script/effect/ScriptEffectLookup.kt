package org.n1.av2.script.effect

import org.n1.av2.script.effect.negative.DecreaseFutureTimersEffectService
import org.n1.av2.script.effect.negative.HiddenEffectsService
import org.n1.av2.script.effect.negative.SpeedUpResetTimerEffectService
import org.n1.av2.script.effect.negative.StartResetTimerEffectService
import org.n1.av2.script.effect.positive.DelayTripwireCountdownService
import org.n1.av2.script.effect.positive.ScanIceNodeEffectService
import org.n1.av2.script.type.ScriptEffectType
import org.n1.av2.script.type.ScriptEffectType.*
import org.springframework.stereotype.Service

@Service
class ScriptEffectLookup(
    decreaseFutureTimersEffectService: DecreaseFutureTimersEffectService,
    speedUpResetTimerEffectService: SpeedUpResetTimerEffectService,
    startResetTimerEffectService: StartResetTimerEffectService,
    hiddenEffectsService: HiddenEffectsService,

    delayTripwireCountdownService: DelayTripwireCountdownService,
    scanIceNodeEffectService: ScanIceNodeEffectService
) {

    private val effectServicesByType = mapOf(
        DECREASE_FUTURE_TIMERS to decreaseFutureTimersEffectService,
        SPEED_UP_RESET_TIMER to speedUpResetTimerEffectService,
        START_RESET_TIMER to startResetTimerEffectService,
        DELAY_TRIPWIRE_COUNTDOWN to delayTripwireCountdownService,
        SCAN_ICE_NODE to scanIceNodeEffectService,
        HIDDEN_EFFECTS to hiddenEffectsService
    )

    fun getForType(type: ScriptEffectType): ScriptEffectInterface {
        return effectServicesByType[type] ?: error("Script effect service not found for type: $type")
    }


}
