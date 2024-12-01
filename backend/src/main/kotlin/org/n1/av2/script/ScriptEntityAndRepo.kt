package org.n1.av2.script

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import java.time.ZonedDateTime

enum class ScriptState { NOT_LOADED, LOADING, LOADED, EXPIRED, USED }

@Document
data class Script(
    @Id val id: String,
    val typeId: String,
    val ownerUserId: String,
    val expiry: ZonedDateTime,
    @Indexed(unique = true) val code: String,
    val state: ScriptState,
    val loadStartedAt: ZonedDateTime?,
    val loadTimeFinishAt: ZonedDateTime?,
)

interface ScriptRepository : CrudRepository<Script, String> {
    fun findByCode(code: String): Script?
    fun findByOwnerUserId(userId: String): List<Script>
}

