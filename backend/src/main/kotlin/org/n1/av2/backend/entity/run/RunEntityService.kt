package org.n1.av2.backend.entity.run

import org.n1.av2.backend.entity.site.LayoutEntityService
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service

@Service
class RunEntityService(
    private val runRepo: RunRepo,
    private val time: TimeService,
    private val currentUserService: CurrentUserService,
    private val layoutEntityService: LayoutEntityService,
) {

    fun getByRunId(runId: String): Run {
        return runRepo.findByRunId(runId) ?: error("${runId} not found")
    }

    fun create(siteId: String, nodeScanById: MutableMap<String, NodeScan>, initiatorId: String): Run {
        val runId = createId("run") { candidate: String -> runRepo.findByRunId(candidate) }

        val run = Run(
            scanStartTime = time.now(),
            runId = runId,
            siteId = siteId,
            nodeScanById = nodeScanById,
            initiatorId = initiatorId
        )
        runRepo.save(run)
        return run
    }

    fun save(run: Run) {
        runRepo.save(run)
    }

    fun getAll(runLinks: List<UserRunLink>): List<Run> {
        val runIds = runLinks.map { it.runId }
        return runRepo.findByRunIdIn(runIds)
    }


}

