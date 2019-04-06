package org.n1.mainframe.backend.repo

import org.n1.mainframe.backend.model.user.User
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepo : PagingAndSortingRepository<User, String> {

    fun findByHandle(handle: String): Optional<User>
}