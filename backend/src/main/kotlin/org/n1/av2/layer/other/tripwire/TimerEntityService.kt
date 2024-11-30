package org.n1.av2.layer.other.tripwire

import org.n1.av2.platform.util.createId
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class TimerEntityService(
    private val repository: TimerRepository,
) {

    fun findByLayer(layerId: String): Timer? {
        return repository.findByLayerId(layerId)
    }

    fun create(layerId: String, userId: String?, finishAt: ZonedDateTime, siteId: String, targetSiteId: String, type: TimerType): Timer {
        val id = createId("timer", repository::findById)

        val timer = Timer(
            id = id,
            layerId = layerId,
            userId = userId,
            finishAt = finishAt,
            siteId = siteId,
            targetSiteId = targetSiteId,
            type = type,
        )

        return repository.save(timer)
    }

    fun deleteById(timerId: String) {
        repository.deleteById(timerId)
    }

    fun deleteByLayerId(layerId: String): Timer? {
        val timer = repository.findByLayerId(layerId) ?: return null
        repository.delete(timer)
        return timer
    }

    fun deleteBySiteId(siteId: String) {
        repository
            .findBySiteId(siteId)
            .forEach { repository.delete(it) }
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

}
