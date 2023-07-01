package org.n1.av2.backend.entity.ice

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime


@Document
data class PasswordIceStatus(
    val id: String,
    @Indexed val layerId: String,
    val attempts: MutableList<String>,
    var lockedUntil: ZonedDateTime,
    val hacked : Boolean = false,
)

@Repository
interface PasswordIceStatusRepo: CrudRepository<PasswordIceStatus, String> {
    fun findByLayerId(layerId: String): PasswordIceStatus?
}
