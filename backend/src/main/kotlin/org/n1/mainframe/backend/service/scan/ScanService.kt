package org.n1.mainframe.backend.service.scan

import org.n1.mainframe.backend.model.scan.NodeScan
import org.n1.mainframe.backend.model.scan.Scan
import org.n1.mainframe.backend.model.scan.UserScan
import org.n1.mainframe.backend.model.site.SiteData
import org.n1.mainframe.backend.model.user.User
import org.n1.mainframe.backend.repo.ScanRepo
import org.n1.mainframe.backend.repo.UserScanRepo
import org.n1.mainframe.backend.service.PrincipalService
import org.n1.mainframe.backend.util.createId
import org.springframework.stereotype.Service

@Service
class ScanService(val scanRepo: ScanRepo,
                  val userScanRepo: UserScanRepo,
                  val principalService: PrincipalService) {
    fun getById(runId: String): Scan {
        return scanRepo.findById(runId).orElseGet { error("${runId} not found") }
    }

    fun createScan(siteData: SiteData, nodeScanById: MutableMap<String, NodeScan>): String {
        val runId = createId("run") { candidate: String -> scanRepo.findById(candidate) }
        val scan = Scan(
                id = runId,
                siteId = siteData.id,
                complete = false,
                nodeScanById = nodeScanById
        )
        scanRepo.save(scan)

        val userId = principalService.get().userId
        val userScan = UserScan(userId, runId )
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
    }

    fun hasUserScan(user: User, runId: String): Boolean {
        return userScanRepo.findByUserIdAndRunId(user.id, runId) != null
    }

    fun createUserScan(runId: String, user: User) {
        val userScan = UserScan(user.id, runId)
        userScanRepo.save(userScan)
    }

    fun deleteUserScan(runId: String) {
        userScanRepo.deleteByUserIdAndRunId(principalService.get().userId, runId)
    }
}

