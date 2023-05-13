package org.n1.av2.backend.entity.user

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(
    @Id val id: String,
    val email: String,
    var name: String = "",
    var type: UserType = UserType.NOT_LOGGED_IN,
    val hacker: Hacker?,
)

data class Hacker(
    val icon: HackerIcon,
    val skill: HackerSkill,
    val playerName: String,
    val characterName: String
)

data class HackerSkill(
    val hacker: Int,
    val elite: Int,
    val architect: Int,
)

// Used for internal activity
val SYSTEM_USER = User(
    id = "user-system",
    email = "no_email@example.com",
    name = "system",
    type = UserType.NOT_LOGGED_IN,
    hacker = null
)