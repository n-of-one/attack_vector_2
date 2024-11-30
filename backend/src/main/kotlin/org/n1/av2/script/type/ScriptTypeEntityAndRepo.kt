package org.n1.av2.script.type

import org.n1.av2.script.effect.ScriptEffect
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import java.math.BigDecimal

@Document
data class ScriptType(
    @Id val id: String,
    val name: String,
    val ram: Int,
    val effects: List<ScriptEffect>,
    val defaultPrice: BigDecimal?,
)

interface ScriptTypeRepository : CrudRepository<ScriptType, String> {
    fun findByName(name: String): ScriptType?
}
