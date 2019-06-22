package org.n1.av2.backend.repo

import org.n1.av2.backend.model.db.run.Run
import org.n1.av2.backend.model.db.run.Scan
import org.n1.av2.backend.model.db.run.UserScan
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface ScanRepo : PagingAndSortingRepository<Scan, String> {
    fun findByIdIn(siteIds: List<String>): List<Scan>
}

@Repository
interface UserScanRepo : PagingAndSortingRepository<UserScan, String> {
    fun findAllByUserId(userId:String): List<UserScan>
    fun findAllByRunId(runId: String): List<UserScan>
    fun findByUserIdAndRunId(userId: String, runId: String): UserScan?
    fun deleteByUserIdAndRunId(userId: String, runId: String)
}

@Repository
interface RunRepo : PagingAndSortingRepository<Run, String>
