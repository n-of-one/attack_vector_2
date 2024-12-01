package org.n1.av2.script.access

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import java.math.BigDecimal


@Document
data class ScriptAccess(
    @Id val id: String,
    val ownerUserId: String,
    val typeId: String,
    val receiveForFree: Int,
    val price: BigDecimal?,
    val used: Boolean, // used this day/event/session.
)

interface ScriptAccessRepository : CrudRepository<ScriptAccess, String> {
    fun findByOwnerUserId(userId: String): List<ScriptAccess>
}
