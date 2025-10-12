package org.n1.av2.script.access

import org.n1.av2.script.type.ScriptTypeId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import java.time.ZonedDateTime

typealias ScriptAccessId = String

@Document
data class ScriptAccess(
    @Id val id: ScriptAccessId,
    val ownerUserId: String,
    val typeId: String,
    val receiveForFree: Int,
    val price: Int?,
    val lastUsed: ZonedDateTime,
)

interface ScriptAccessRepository : CrudRepository<ScriptAccess, ScriptAccessId> {
    fun findByOwnerUserId(userId: String): List<ScriptAccess>
    fun findByTypeId(typeId: ScriptTypeId): List<ScriptAccess>
}
