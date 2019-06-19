package org.n1.av2.backend.service.scan

import org.n1.av2.backend.model.db.run.NodeScan
import org.n1.av2.backend.model.db.run.Scan
import org.n1.av2.backend.model.db.run.UserScan
import org.n1.av2.backend.model.db.site.SiteData
import org.n1.av2.backend.model.db.user.User
import org.n1.av2.backend.repo.ScanRepo
import org.n1.av2.backend.repo.UserScanRepo
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service

@Service
class ScanService(val scanRepo: ScanRepo,
                  val userScanRepo: UserScanRepo,
                  val currentUserService: CurrentUserService,
                  val time: TimeService) {
    fun getById(runId: String): Scan {
        return scanRepo.findById(runId).orElseGet { error("${runId} not found") }
    }

    fun createScan(siteData: SiteData, nodeScanById: MutableMap<String, NodeScan>, user: User): String {
        val runId = createId("run") { candidate: String -> scanRepo.findById(candidate) }
        val scan = Scan(
                id = runId,
                siteId = siteData.id,
                complete = false,
                nodeScanById = nodeScanById,
                initiatorId =  user.id,
                startTime = time.now()
        )
        scanRepo.save(scan)

        val userId = currentUserService.userId
        val userScan = UserScan(userId, runId)
        userScanRepo.save(userScan)

        return runId
    }

    fun save(scan: Scan) {
        scanRepo.save(scan)
    }

    fun getAll(userId: String): List<Scan> {
        val userScans = userScanRepo.findAllByUserId(userId)
        val runIds = userScans.map { it.runId }
        return scanRepo.findByIdIn(runIds)
    }

    fun purgeAll() {
        scanRepo.deleteAll()
        userScanRepo.deleteAll()
    }

    fun hasUserScan(user: User, runId: String): Boolean {
        return userScanRepo.findByUserIdAndRunId(user.id, runId) != null
    }

    fun createUserScan(runId: String, user: User) {
        val userScan = UserScan(user.id, runId)
        userScanRepo.save(userScan)
    }

    fun deleteUserScan(runId: String) {
        userScanRepo.deleteByUserIdAndRunId(currentUserService.userId, runId)
    }
}

