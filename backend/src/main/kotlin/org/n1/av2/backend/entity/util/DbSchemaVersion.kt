package org.n1.av2.backend.entity.util

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Document
data class DbSchemaVersion(
    val version: Int,
    val description: String,
    val appliedAt: ZonedDateTime,
)

@Repository
interface DbSchemaVersionRepository : MongoRepository<DbSchemaVersion, Int> {
}
