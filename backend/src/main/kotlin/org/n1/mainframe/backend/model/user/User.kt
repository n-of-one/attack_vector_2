package org.n1.mainframe.backend.model.user

import org.n1.mainframe.backend.model.user.enums.UserType

data class User (val id: String,
                 var failedLoginCount: Int = 0,
                 var blockedUntil: Long = 0,
                 var name:String = "",
                 var encodedPasscoded:String = "",
                 var type: UserType = UserType.NOT_LOGGED_IN)
