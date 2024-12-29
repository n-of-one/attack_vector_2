package org.n1.av2.script

import org.n1.av2.script.type.ScriptType
import org.n1.av2.script.type.ScriptTypeId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import java.time.ZonedDateTime

enum class ScriptState { AVAILABLE, LOADING, LOADED, USED, EXPIRED }


typealias ScriptId = String

@Document
data class Script(
    @Id val id: ScriptId,
    val typeId: String,
    val ownerUserId: String,
    val expiry: ZonedDateTime,
    @Indexed(unique = true) val code: String,
    val state: ScriptState,
    val inMemory: Boolean,
    val loadStartedAt: ZonedDateTime?,
    val loadTimeFinishAt: ZonedDateTime?,
)

interface ScriptRepository : CrudRepository<Script, ScriptId> {
    fun findByCode(code: String): Script?
    fun findByOwnerUserId(userId: String): List<Script>
    fun findByTypeId(typeId: ScriptTypeId): List<Script>
}

