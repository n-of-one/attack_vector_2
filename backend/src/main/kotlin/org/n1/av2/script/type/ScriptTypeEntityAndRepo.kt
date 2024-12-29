package org.n1.av2.script.type

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.script.effect.ScriptEffectService
import org.n1.av2.script.effect.negative.DecreaseFutureTimersEffectService
import org.n1.av2.script.effect.negative.SpeedUpResetTimerEffectService
import org.n1.av2.script.effect.negative.StartResetTimerEffectService
import org.n1.av2.script.effect.positive.DelayTripwireCountdownService
import org.n1.av2.script.effect.positive.ScanIceNodeEffectService
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import java.math.BigDecimal
import kotlin.reflect.KClass

typealias ScriptTypeId = String

@Document
data class ScriptType(
    @Id val id: ScriptTypeId,
    val name: String,
    val ram: Int,
    val effects: List<ScriptEffect>,
    val defaultPrice: BigDecimal?,
)

data class ScriptEffect(
    val type: ScriptEffectType,
    val value: String?,
)

enum class ScriptEffectType(
    val effectServiceClass: KClass<out ScriptEffectService>,
) {
    DELAY_TRIPWIRE_COUNTDOWN(DelayTripwireCountdownService::class),
    SCAN_ICE_NODE( ScanIceNodeEffectService::class),

    START_RESET_TIMER(StartResetTimerEffectService::class),
    SPEED_UP_RESET_TIMER(SpeedUpResetTimerEffectService::class),
    DECREASE_FUTURE_TIMERS( DecreaseFutureTimersEffectService::class),
}

interface ScriptTypeRepository : CrudRepository<ScriptType, ScriptTypeId> {
    fun findByName(name: String): ScriptType?
}
