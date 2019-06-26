package org.n1.av2.backend.service.run

import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.repo.HackerPositionRepo
import org.springframework.stereotype.Service

@Service
class HackerPositionService(
        val hackerPositionRepo: HackerPositionRepo) {

    fun getByRunIdAndUserId(runId: String, userId: String): HackerPosition {
        return hackerPositionRepo.findByRunIdAndUserId(runId, userId) ?: error("HackerPosition not found for ${runId} - ${userId}")
    }

    fun saveInTransit(position: HackerPosition) {
        val newPosition = position.copy(inTransit = true)
        hackerPositionRepo.save(newPosition)
    }
}