package org.n1.av2.backend.service.run

import mu.KLogging
import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.repo.HackerPositionRepo
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.service.site.SiteDataService
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

    fun retrieveForCurrentUser(): HackerPosition {
        return hackerPositionRepo.findByUserId(currentUserService.userId) ?: error("HackerPosition not found for ${currentUserService.userId}")
    }

    fun retrieve(userId: String): HackerPosition {
        return hackerPositionRepo.findByUserId(userId) ?: error("HackerPosition not found for ${userId}")
    }

    fun startRun(runId: String) {
        val userId = currentUserService.userId
        val scan = scanService.getByRunId(runId)
        val siteData = siteDataService.getBySiteId(scan.siteId)
        val nodes = nodeService.getAll(scan.siteId)
        val startNode = nodes.find{it.networkId == siteData.startNodeNetworkId}!!

        val newPosition = HackerPosition(
                runId = runId,
                siteId = scan.siteId,
                userId = userId,
                currentNodeId = startNode.id,
                previousNodeId = startNode.networkId,
                inTransit = true,
                locked = false)
        hackerPositionRepo.save(newPosition)
    }

    fun saveInTransit(position: HackerPosition) {
        val newPosition = position.copy(inTransit = true)
        hackerPositionRepo.save(newPosition)
    }

    fun arriveAt(nodeId: String) {
        val position = retrieveForCurrentUser()

        val newPosition = position.copy(inTransit = false, currentNodeId = nodeId, previousNodeId = position.currentNodeId)
        hackerPositionRepo.save(newPosition)
    }

    fun purgeAll() {
        hackerPositionRepo.deleteAll()
    }

    fun lockHacker() {
        val position = retrieveForCurrentUser()
        val newPosition = position.copy(locked = true)
        hackerPositionRepo.save(newPosition)
    }

}