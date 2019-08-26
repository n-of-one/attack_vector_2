package org.n1.av2.backend.repo

import org.n1.av2.backend.model.db.run.*
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface ScanRepo : PagingAndSortingRepository<Scan, String> {
    fun findByRunIdIn(siteIds: List<String>): List<Scan>
    fun findByRunId(runId: String): Scan?
}

@Repository
interface UserScanRepo : PagingAndSortingRepository<UserScan, String> {
    fun findAllByUserId(userId:String): List<UserScan>
    fun findAllByRunId(runId: String): List<UserScan>
    fun findByUserIdAndRunId(userId: String, runId: String): UserScan?
    fun deleteByUserIdAndRunId(userId: String, runId: String)
}


@Repository
interface HackerPositionRepo : PagingAndSortingRepository<HackerPosition, String> {
    fun findByUserId(userId: String): HackerPosition?
}

@Repository
interface ServiceStatusRepo: PagingAndSortingRepository<ServiceStatus, String> {
    fun findByServiceIdAndRunId(serviceId: String, runId: String): ServiceStatus?
}

@Repository
interface IcePasswordStatusRepo: PagingAndSortingRepository<IcePasswordStatus, String> {
    fun findByServiceIdAndRunId(serviceId: String, runId: String): IcePasswordStatus?
}
