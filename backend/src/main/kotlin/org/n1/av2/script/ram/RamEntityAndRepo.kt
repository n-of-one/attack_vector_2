package org.n1.av2.script.ram

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

typealias RamId = String

@Document
data class RamEntity(
    @Id val id: RamId,
    @Indexed val userId: String,
    val enabled: Boolean,
    val size: Int,
    val loaded: Int,
    val refreshing: Int,
    val free: Int,
    val nextRefresh: ZonedDateTime?,
    val lockedUntil: ZonedDateTime?,
)

@Repository
interface RamRepository : CrudRepository<RamEntity, RamId> {
    fun findByUserId(userId: String): RamEntity?
}
