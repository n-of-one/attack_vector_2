package org.n1.av2.script.type

import org.n1.av2.script.effect.ScriptEffectType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository

typealias ScriptTypeId = String

@Document
data class ScriptType(
    @Id val id: ScriptTypeId,
    val name: String,
    val gmNote: String,
    val category: String,
    val size: Int,
    val effects: List<ScriptEffect>,
    val defaultPrice: Int?,
)

data class ScriptEffect(
    val type: ScriptEffectType,
    val value: String?,
)

interface ScriptTypeRepository : CrudRepository<ScriptType, ScriptTypeId> {
    fun findByNameIgnoreCase(name: String): ScriptType?
}
