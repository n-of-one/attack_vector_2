package org.n1.av2.script.type

import org.n1.av2.script.effect.ScriptEffectType
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

interface ScriptTypeRepository : CrudRepository<ScriptType, ScriptTypeId> {
}
