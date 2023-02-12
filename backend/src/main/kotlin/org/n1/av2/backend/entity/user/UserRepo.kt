package org.n1.av2.backend.entity.user

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepo : CrudRepository<User, String> {

    fun findByNameIgnoreCase(userName: String): User?
}