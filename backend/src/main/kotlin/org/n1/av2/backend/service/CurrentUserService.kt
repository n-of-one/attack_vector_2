package org.n1.av2.backend.service

import org.n1.av2.backend.model.db.user.User
import org.n1.av2.backend.model.iam.UserPrincipal
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class CurrentUserService {
    private val principalStore = ThreadLocal<UserPrincipal>()

    fun set(principal: Principal) {
        if (principal is UserPrincipal) {
            principalStore.set(principal)
        }
        else {
            throw IllegalArgumentException("Principal is not a UserPrincipal, but a ${principal.javaClass} and has value: ${principal}")
        }
    }

    val principal: UserPrincipal
    get() {
        return principalStore.get()
    }

    val user: User
    get () {
        return principalStore.get().user
    }

    val userId: String
        get () {
            return principalStore.get().userId
        }

    fun remove() {
        principalStore.remove()
    }


}