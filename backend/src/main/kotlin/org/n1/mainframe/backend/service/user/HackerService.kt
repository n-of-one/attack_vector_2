package org.n1.mainframe.backend.service.user

import org.n1.mainframe.backend.model.hacker.HackerActivityType
import org.n1.mainframe.backend.model.hacker.HackerPresence
import org.springframework.stereotype.Service

@Service
class HackerService(val userService: UserService,
        val hackerActivityService: HackerActivityService) {

    fun getUserPresence(type: HackerActivityType, id: String): List<HackerPresence> {
        return hackerActivityService.getAll(type, id)
                .map{ activity -> createPresence(activity.userName)}

    }

    private fun createPresence(userName: String): HackerPresence {
        val user = userService.getUserByUserName(userName)
        return HackerPresence(user.id, userName, "SCORPION")
    }
}