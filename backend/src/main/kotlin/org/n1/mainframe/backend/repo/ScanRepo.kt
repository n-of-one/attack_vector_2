package org.n1.mainframe.backend.repo

import org.n1.mainframe.backend.model.scan.Scan
import org.n1.mainframe.backend.model.scan.UserScan
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ScanRepo : PagingAndSortingRepository<Scan, String> {
    fun findBySiteId(siteId:String ): Scan
    fun findByIdIn(siteIds: List<String>): List<Scan>
}

@Repository
interface UserScanRepo : PagingAndSortingRepository<UserScan, String> {
    fun findAllByUserId(userId:String): List<UserScan>
    fun findByUserIdAndScanId(userId: String, scanId: String): Optional<UserScan>
    fun deleteByUserIdAndScanId(userId: String, scanId: String)
}
