package org.n1.av2.script

import org.n1.av2.script.type.ScriptTypeId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import java.time.ZonedDateTime

enum class ScriptState {
    AVAILABLE, // the script is owned by the hacker
    LOADED,    // the script is loaded in memory and can be used in a run
    USED,      // the script has been used in a run, cannot be used again.
    EXPIRED,   // the script has expired and cannot be used
    OFFERING,  // the hacker has marked this script to be available for other hackers to retrieve.
}

typealias ScriptId = String

@Document
data class Script(
    @Id val id: ScriptId,
    val typeId: String,
    val ownerUserId: String,
    val expiry: ZonedDateTime,
    @Indexed(unique = true) val code: String,
    val state: ScriptState,
)

interface ScriptRepository : CrudRepository<Script, ScriptId> {
    fun findByCode(code: String): Script?
    fun findByOwnerUserId(userId: String): List<Script>
    fun findByTypeId(typeId: ScriptTypeId): List<Script>
}
