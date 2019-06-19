package org.n1.av2.backend.service.run

import org.n1.av2.backend.model.db.run.Run
import org.n1.av2.backend.repo.RunRepo
import org.n1.av2.backend.service.TimeService
import org.springframework.stereotype.Service

@Service
class RunService(
        val runRepo: RunRepo,
        val time: TimeService
) {
    fun get(id: String): Run {
        return runRepo.findById(id).orElseThrow{ error("Run with id ${id} not found.")}
    }

    fun start(id: String) {
        val run = get(id)

        run.startTime = time.now()

    }
}