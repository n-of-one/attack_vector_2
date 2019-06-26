package org.n1.av2.backend.service.run

import mu.KLogging
import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.repo.HackerPositionRepo
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.service.site.SiteDataService
import org.n1.av2.backend.util.logNanoTime
import org.springframework.stereotype.Service

@Service
class HackerPositionService(
        val hackerPositionRepo: HackerPositionRepo,
        val currentUserService: CurrentUserService,
        val siteDataService: SiteDataService,
        val nodeService: NodeService,
        val scanService: ScanService
) {

    companion object: KLogging()

    fun getByRunIdAndUserId(runId: String, userId: String): HackerPosition {
        return hackerPositionRepo.findByRunIdAndUserId(runId, userId) ?: error("HackerPosition not found for ${runId} - ${userId}")
    }

    fun startRun(runId: String) {
        val userId = currentUserService.userId
        val position = hackerPositionRepo.findByRunIdAndUserId(runId, userId) ?: createNewPosition(runId, userId)

        val newPosition = position.copy(inTransit = true)
        hackerPositionRepo.save(newPosition)
    }

    private fun createNewPosition(runId: String, userId: String): HackerPosition {
        return logNanoTime("createNewPosition", logger) {
            val scan = scanService.getByRunId(runId)
            val siteData = siteDataService.getBySiteId(scan.siteId)
            val nodes = nodeService.getAll(scan.siteId)
            val startNode = nodes.find{it.networkId == siteData.startNodeNetworkId}!!

            HackerPosition(
                    runId = runId,
                    siteId = scan.siteId,
                    userId = userId,
                    currentNodeId = startNode.id,
                    previousNodeId = startNode.networkId,
                    inTransit = true)
        }
    }
}