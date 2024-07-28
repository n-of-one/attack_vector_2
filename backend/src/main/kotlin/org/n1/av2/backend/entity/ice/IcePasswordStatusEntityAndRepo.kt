package org.n1.av2.backend.entity.ice

import org.n1.av2.backend.entity.site.enums.LayerType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime


@Document
data class IcePasswordStatus(
    @Id val id: String, // iceId
    @Indexed(unique = true) val layerId: String,
    val hackerAttempts: List<String>,  // only attempts by hackers are tracked
    val attemptCount: Int = 0,         // but all attempts count for the purpose of locking
    val lockedUntil: ZonedDateTime,
    val authorized: List<String> = emptyList() // userIds of users who are authorized to pass
)

@Repository
interface IcePasswordStatusRepo: CrudRepository<IcePasswordStatus, String> {
    fun findByLayerId(layerId: String): IcePasswordStatus?
}


fun String.determineIceType(): LayerType {
    val iceIdPrefix = this.split("-")[0]
    return when (iceIdPrefix) {
        "password" -> LayerType.PASSWORD_ICE
        "tangle" -> LayerType.TANGLE_ICE
        "netwalk" -> LayerType.NETWALK_ICE
        "wordSearch" -> LayerType.WORD_SEARCH_ICE
        "tar" -> LayerType.TAR_ICE
        else -> error("Unknown ice type for ID: ${this}")
    }
}