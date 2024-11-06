package org.n1.av2.platform.iam.user

import org.n1.av2.platform.inputvalidation.UserName
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


enum class UserTag(
    val changeable: Boolean,
    val description: String,
) {
    REGUlAR(true, "regular user"),                   // Regular users (that can be renamed or deleted)
    MANDATORY(false, "a mandatory (default) user"),        // Default user that is created by the system, such as the default admin and GM users
    SKILL_TEMPLATE(false, "the skill template user"),         // The hacker user that is used as the template for skills of new hackers
    EXTERNAL_SYSTEM(false, "a user representing an external system"), // Dedicated users that represent external systems such as Lola for Frontier
}

@Document
data class UserEntity(
    @Id val id: String,
    @field:UserName var name: String = "",
    var type: UserType = UserType.NOT_LOGGED_IN,
    val externalId: String? = null,
    val tag: UserTag = UserTag.REGUlAR,
)


@Repository
interface UserEntityRepo : CrudRepository<UserEntity, String> {
    fun findByNameIgnoreCase(userName: String): UserEntity?
    fun findByExternalId(externalId: String): UserEntity?
}


// Used for internal activity
val SYSTEM_USER = UserEntity(
    id = "user-system",
    externalId = "user-system",
    name = "system",
    type = UserType.NOT_LOGGED_IN,
)

val NOT_LOGGED_IN_USER = UserEntity(
    id = "user-not-logged-in",
    externalId = "user-not-logged-in",
    name = "notLoggedIn",
    type = UserType.NOT_LOGGED_IN,
)
