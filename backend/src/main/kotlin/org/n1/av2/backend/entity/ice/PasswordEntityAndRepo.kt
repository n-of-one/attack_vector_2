package org.n1.av2.backend.entity.ice

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime


data class PasswordIceStatus(
    val id: String,
    val runId: String,
    val nodeId: String,
    val layerId: String,
    val attempts: MutableList<String>,
    var lockedUntil: ZonedDateTime,
    val hacked : Boolean = false,
)

@Repository
interface PasswordIceStatusRepo: CrudRepository<PasswordIceStatus, String> {
}
