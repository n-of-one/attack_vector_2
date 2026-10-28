package org.n1.av2.timer

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.ZonedDateTime

enum class TimerEffect { SHUTDOWN_START, SHUTDOWN_FINISH }
enum class TimerLabel { TRIPWIRE_SITE_SHUTDOWN, SCRIPT_SITE_SHUTDOWN }

@Document
data class Timer(
    @Id val id: String,
    val layerId: String?, // layerId of the tripwire that caused the timer
    val siteId: String, // site that 'runs' the timer, meaning that it can be stopped from that site
    val targetSiteId: String, // site that is targeted by the effect of the timer
    val userId: String?,
    val finishAt: ZonedDateTime,
    val effect: TimerEffect,
    val label: TimerLabel? = TimerLabel.TRIPWIRE_SITE_SHUTDOWN,
    val effectDuration: Duration,
)

@Repository
interface TimerRepository : CrudRepository<Timer, String> {
    fun findByLayerId(layerId: String): Timer?
    fun findBySiteId(siteId: String): List<Timer>
    fun findByTargetSiteId(targetSiteId: String): List<Timer>
    fun findByUserId(userId: String): List<Timer>
}
