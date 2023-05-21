package org.n1.av2.backend.entity.user

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Document
data class User(
    @Id val id: String,
    val externalId: String? = null,
    val email: String,
    var name: String = "",
    var type: UserType = UserType.NOT_LOGGED_IN,
    val hacker: Hacker?,
    val gmNote: String = "",
)


@Repository
interface UserRepo : CrudRepository<User, String> {
    fun findByNameIgnoreCase(userName: String): User?
    fun findByExternalId(externalId: String): User?
}

data class Hacker(
    val icon: HackerIcon,
    val skill: HackerSkill,
    val characterName: String,
)

data class HackerSkill(
    val hacker: Int,
    val elite: Int,
    val architect: Int,
)

// Used for internal activity
val SYSTEM_USER = User(
    id = "user-system",
    externalId = "user-system",
    email = "no_email@example.com",
    name = "system",
    type = UserType.NOT_LOGGED_IN,
    hacker = null
)