package org.n1.av2.script.type

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import java.math.BigDecimal

typealias ScriptTypeId = String

@Document
data class ScriptType(
    @Id val id: ScriptTypeId,
    val name: String,
    val size: Int,
    val effects: List<ScriptEffect>,
    val defaultPrice: BigDecimal?,
)

data class ScriptEffect(
    val type: ScriptEffectType,
    val value: String?,
)

enum class ScriptEffectType{
    DELAY_TRIPWIRE_COUNTDOWN,
    SCAN_ICE_NODE,
    START_RESET_TIMER,
    SPEED_UP_RESET_TIMER,
    DECREASE_FUTURE_TIMERS,
    HIDDEN_EFFECTS,
    SITE_STATS,
    JUMP_TO_NODE,
}

interface ScriptTypeRepository : CrudRepository<ScriptType, ScriptTypeId> {
}
