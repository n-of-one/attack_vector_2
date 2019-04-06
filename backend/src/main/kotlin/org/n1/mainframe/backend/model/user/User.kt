package org.n1.mainframe.backend.model.user

import org.n1.mainframe.backend.model.user.enums.HackerSpecialization
import org.n1.mainframe.backend.model.user.enums.UserType

data class User (val id: String,
            var failedLoginCount: Int = 0,
            var blockedUntil: Long = 0,
            var handle:String = "",
            var ocName:String = "Not logged in",
            var encodedPasscoded:String = "",
            var type: UserType = UserType.NOT_LOGGED_IN,
            var icName: String = "",
            var skill_it: Int = 0,
            var specialization: HackerSpecialization = HackerSpecialization.NONE)
