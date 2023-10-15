package org.n1.av2.backend.entity.service

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

enum class TimerType { SHUTDOWN_START, SHUTDOWN_END }

@Document
data class Timer(
    @Id val id: String,
    val layerId: String,
    val siteId: String,
    val targetSiteId: String,
    val userId: String?,
    val finishAt: ZonedDateTime,
    val type: TimerType,
)

@Repository
interface TimerRepository : CrudRepository<Timer, String> {
    fun findByLayerId(layerId: String): Timer?
    fun findBySiteId(siteId: String): List<Timer>
    fun findByTargetSiteId(targetSiteId: String): List<Timer>
    fun findByUserId(userId: String): List<Timer>
}