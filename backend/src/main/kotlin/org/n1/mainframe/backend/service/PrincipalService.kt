package org.n1.mainframe.backend.service

import org.n1.mainframe.backend.model.iam.UserPrincipal
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class PrincipalService {
    private val principalStore = ThreadLocal<UserPrincipal>()

    fun set(principal: Principal) {
        if (principal is UserPrincipal) {
            principalStore.set(principal)
        }
        else {
            throw IllegalArgumentException("Principal is not a UserPrincipal, but a ${principal.javaClass} and has value: ${principal}")
        }
    }

    fun get(): UserPrincipal {
        return principalStore.get()
    }

    fun remove() {
        principalStore.remove()
    }


}