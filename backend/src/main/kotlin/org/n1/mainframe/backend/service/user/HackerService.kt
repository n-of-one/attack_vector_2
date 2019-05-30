package org.n1.mainframe.backend.service.user

import org.n1.mainframe.backend.model.hacker.HackerActivityType
import org.n1.mainframe.backend.model.hacker.HackerPresence
import org.n1.mainframe.backend.model.user.User
import org.springframework.stereotype.Service

@Service
class HackerService(val userService: UserService,
        val hackerActivityService: HackerActivityService) {

    fun getUserPresence(type: HackerActivityType, id: String): List<HackerPresence> {
        return hackerActivityService.getAll(type, id)
                .map{ activity -> createPresence(activity.authentication.user)}

    }

    private fun createPresence(user: User): HackerPresence {
        return HackerPresence(user.id, user.name, user.icon)
    }
}