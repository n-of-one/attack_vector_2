package org.n1.av2.backend.entity.user

import org.n1.av2.backend.model.validation.UserName
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Document
data class UserEntity(
    @Id val id: String,
    val externalId: String? = null,
    val email: String,
    @field:UserName var name: String = "",
    var type: UserType = UserType.NOT_LOGGED_IN,
    val hacker: Hacker?,
    val gmNote: String = "",
)


@Repository
interface UserRepo : CrudRepository<UserEntity, String> {
    fun findByNameIgnoreCase(userName: String): UserEntity?
    fun findByExternalId(externalId: String): UserEntity?
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
val SYSTEM_USER = UserEntity(
    id = "user-system",
    externalId = "user-system",
    email = "no_email@example.com",
    name = "system",
    type = UserType.NOT_LOGGED_IN,
    hacker = null
)