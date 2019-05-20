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
    fun getById(scanId: String): Scan {
        return scanRepo.findById(scanId).orElseGet { error("${scanId} not found") }
    }

    fun createScan(siteData: SiteData, nodeScanById: MutableMap<String, NodeScan>): String {
        val scanId = createId("scan") { candidate: String -> scanRepo.findById(candidate) }
        val scan = Scan(
                id = scanId,
                siteId = siteData.id,
                complete = false,
                nodeScanById = nodeScanById
        )
        scanRepo.save(scan)

        val userId = principalService.get().userId
        val userScan = UserScan(userId, scanId )
        userScanRepo.save(userScan)

        return scanId
    }

    fun save(scan: Scan) {
        scanRepo.save(scan)
    }

    fun getAll(userId: String): List<Scan> {
        val userScans = userScanRepo.findAllByUserId(userId)
        val scanIds = userScans.map { it.scanId }
        return scanRepo.findByIdIn(scanIds)
    }

    fun purgeAll() {
        scanRepo.deleteAll()
    }

    fun hasUserScan(user: User, scanId: String): Boolean {
        val userScan = userScanRepo.findByUserIdAndScanId(user.id, scanId)
        return userScan.isPresent
    }

    fun createUserScan(scanId: String, user: User) {
        val userScan = UserScan(user.id, scanId)
        userScanRepo.save(userScan)
    }
}

