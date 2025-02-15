package org.n1.av2.timer

import org.n1.av2.platform.util.createId
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZonedDateTime

@Service
class TimerEntityService(
    private val repository: TimerRepository,
) {

    fun findByLayer(layerId: String): Timer? {
        return repository.findByLayerId(layerId)
    }

    fun findAll(): List<Timer> {
        return repository.findAll().toList()
    }

    fun create(
        layerId: String?, userId: String?, finishAt: ZonedDateTime, siteId: String, targetSiteId: String,
        effect: TimerEffect, effectDuration: Duration, label: TimerLabel? = null
    ): Timer {
        val id = createId("timer", repository::findById)

        val timer = Timer(
            id = id,
            layerId = layerId,
            userId = userId,
            finishAt = finishAt,
            siteId = siteId,
            targetSiteId = targetSiteId,
            effect = effect,
            effectDuration = effectDuration,
            label = label,
        )

        return repository.save(timer)
    }

    fun deleteById(timerId: String) {
        repository.deleteById(timerId)
    }

    fun deleteBySiteId(siteId: String): List<Timer> {
        val timers = repository.findBySiteId(siteId)
        timers.forEach { repository.delete(it) }
        return timers
    }

    fun findForEnterSite(siteId: String, userId: String): List<Timer> {
        val forSite = repository.findBySiteId(siteId)
        val forTargetSite = repository.findByTargetSiteId(siteId)
        val forUser = repository.findByUserId(userId)
        return (forSite + forTargetSite + forUser).distinctBy { it.id }
    }

    fun update(timer: Timer) {
        repository.save(timer)
    }

    fun findByTargetSiteId(siteId: String): List<Timer> {
        return repository.findByTargetSiteId(siteId)
    }

}
