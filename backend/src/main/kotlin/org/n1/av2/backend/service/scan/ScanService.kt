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
class ScanService(private val scanRepo: ScanRepo,
                  private val userScanRepo: UserScanRepo,
                  private val currentUserService: CurrentUserService) {

    fun getByRunId(runId: String): Scan {
        return scanRepo.findByRunId(runId) ?: error("${runId} not found")
    }

    fun createScan(siteData: SiteData, nodeScanById: MutableMap<String, NodeScan>, user: User): String {
        val runId = createId("run") { candidate: String -> scanRepo.findByRunId(candidate) }
        val scan = Scan(
                runId = runId,
                siteId = siteData.siteId,
                nodeScanById = nodeScanById,
                initiatorId =  user.id
        )
        scanRepo.save(scan)

        val userId = currentUserService.userId
        val userScan = UserScan(userId = userId, runId = runId)
        userScanRepo.save(userScan)

        return runId
    }

    fun save(scan: Scan) {
        scanRepo.save(scan)
    }

    fun getAll(userId: String): List<Scan> {
        val userScans = userScanRepo.findAllByUserId(userId)
        val runIds = userScans.map { it.runId }
        return scanRepo.findByRunIdIn(runIds)
    }

    fun purgeAll() {
        scanRepo.deleteAll()
        userScanRepo.deleteAll()
    }

    fun hasUserScan(user: User, runId: String): Boolean {
        return userScanRepo.findByUserIdAndRunId(user.id, runId) != null
    }

    fun createUserScan(runId: String, user: User) {
        val userScan = UserScan(userId = user.id, runId = runId)
        userScanRepo.save(userScan)
    }

    fun deleteUserScan(runId: String) {
        userScanRepo.deleteByUserIdAndRunId(currentUserService.userId, runId)
    }

    fun getUsersOfScan(runId: String): Collection<String> {
        val userScans = userScanRepo.findAllByRunId(runId)
        return userScans.map { it.userId }
    }
}

