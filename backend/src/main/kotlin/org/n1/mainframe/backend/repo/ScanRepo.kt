package org.n1.mainframe.backend.repo

import org.n1.mainframe.backend.model.scan.Scan
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface ScanRepo : PagingAndSortingRepository<Scan, String> {
    fun findBySiteId(siteId:String ): Scan
    fun findByIdIn(siteIds: List<String>): List<Scan>
}
