package org.n1.av2.backend.entity.user

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User (
        @Id val id: String,
        var failedLoginCount: Int = 0,
        var blockedUntil: Long = 0,
        var name:String = "",
        var encodedPasscoded:String = "",
        var type: UserType = UserType.NOT_LOGGED_IN,
        var icon: HackerIcon
)


