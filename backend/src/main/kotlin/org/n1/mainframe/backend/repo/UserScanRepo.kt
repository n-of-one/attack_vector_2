package org.n1.mainframe.backend.repo

import org.n1.mainframe.backend.model.scan.UserScan
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserScanRepo : PagingAndSortingRepository<UserScan, String> {
    fun findAllByUserId(userId:String): List<UserScan>
    fun findByUserIdAndScanId(userId: String, scanId: String): Optional<UserScan>
}
