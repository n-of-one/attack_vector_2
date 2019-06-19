package org.n1.av2.backend.repo

import org.n1.av2.backend.model.db.user.User
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepo : PagingAndSortingRepository<User, String> {

    fun findByNameIgnoreCase(userName: String): User?
}