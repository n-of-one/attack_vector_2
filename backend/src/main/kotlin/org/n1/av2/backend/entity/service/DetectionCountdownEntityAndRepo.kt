package org.n1.av2.backend.entity.service

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Document
data class DetectionCountdown(
    val id: String,
    val layerId: String,
    val siteId: String,
    val targetSiteId: String,
    val userId: String?,
    val finishAt: ZonedDateTime,
)

@Repository
interface DetectionCountdownRepository: CrudRepository<DetectionCountdown, String> {
    fun findByLayerId(layerId: String): DetectionCountdown?
    fun findBySiteId(siteId: String): List<DetectionCountdown>
    fun findByTargetSiteId(targetSiteId: String): List<DetectionCountdown>
    fun findByUserId(userId: String): List<DetectionCountdown>
}