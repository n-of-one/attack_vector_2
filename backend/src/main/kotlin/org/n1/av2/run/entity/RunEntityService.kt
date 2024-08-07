package org.n1.av2.run.entity

import org.n1.av2.platform.util.createId
import org.n1.av2.run.runlink.RunLink
import org.springframework.stereotype.Service

@Service
class RunEntityService(
    private val runRepo: RunRepo,
) {

    fun getByRunId(runId: String): Run {
        return runRepo.findByRunId(runId) ?: error("${runId} not found")
    }

    fun create(siteId: String, nodeScanById: MutableMap<String, NodeScan>, initiatorId: String): Run {
        val runId =  createId("run") { candidate: String -> runRepo.findByRunId(candidate) }

        val run = Run(
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

    fun getAll(runLinks: List<RunLink>): List<Run> {
        val runIds = runLinks.map { it.runId }
        return runRepo.findByRunIdIn(runIds)
    }

    fun delete(run: Run) {
        runRepo.delete(run)
    }

    fun findAllForSiteId(siteId: String): List<Run> {
        return runRepo.findBySiteId(siteId)
    }


}

