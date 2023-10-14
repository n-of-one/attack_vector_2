package org.n1.av2.backend.entity.service

import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class DetectionCountdownEntityService(
    private val repository: DetectionCountdownRepository
) {

    fun findByLayer(layerId: String): DetectionCountdown? {
        return repository.findByLayerId(layerId)
    }

    fun create(layerId: String, userId: String?, finishAt: ZonedDateTime, siteId: String, targetSiteId: String): DetectionCountdown {
        val id = createId("detection", repository::findById)

        val detectionCountdown = DetectionCountdown(
            id=id,
            layerId=layerId,
            userId=userId,
            finishAt=finishAt,
            siteId= siteId,
            targetSiteId = targetSiteId
        )

        return repository.save(detectionCountdown)
    }

    fun deleteByLayerId(layerId: String) {
        val detection = repository.findByLayerId(layerId) ?: return
        repository.delete(detection)
    }

    fun deleteBySiteId(siteId: String) {
        repository
            .findBySiteId(siteId)
            .forEach { repository.delete(it) }
    }

    fun findForEnterSite(siteId: String, userId: String): List<DetectionCountdown> {
        val forSite = repository.findBySiteId(siteId)
        val forTargetSite = repository.findByTargetSiteId(siteId)
        val forUser = repository.findByUserId(userId)
        return (forSite + forTargetSite + forUser).distinctBy { it.id }
    }

}