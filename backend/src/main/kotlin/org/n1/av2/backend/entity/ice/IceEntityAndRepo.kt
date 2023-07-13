package org.n1.av2.backend.entity.ice

import org.n1.av2.backend.entity.site.enums.LayerType
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime


@Document
data class IceStatus(
    val id: String,
    @Indexed val layerId: String,
    val hackerAttempts: List<String>,  // only attempts by hackers are tracked
    val attemptCount: Int = 0,         // but all attempts count for the purpose of locking
    val lockedUntil: ZonedDateTime,
    val hacked : Boolean = false,
)

@Repository
interface IceStatusRepo: CrudRepository<IceStatus, String> {
    fun findByLayerId(layerId: String): IceStatus?
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