package org.n1.av2.backend.service

import org.n1.av2.backend.engine.SYSTEM_USER_ID
import org.n1.av2.backend.model.db.user.User
import org.n1.av2.backend.model.db.user.UserType.ADMIN
import org.springframework.stereotype.Service

@Service
class CurrentUserService {
    private val userStore = ThreadLocal<User>()

    private val logger = mu.KotlinLogging.logger {}

    fun set(user: User) {
        userStore.set(user)
    }

    val user: User
        get () {
            return userStore.get()
        }

    val userId: String
        get () {
            val userId = userStore.get().id
            if (userId == SYSTEM_USER_ID) error("Tried to access the current user from a game event context.")
            return userId
        }

    val isSystemUser: Boolean
        get() {
            val user = userStore.get() ?: return true
            return user.id == SYSTEM_USER_ID
        }

    val isAdmin: Boolean
        get() {
            val user = userStore.get() ?: return false
            return user.type == ADMIN
        }

    fun remove() {
        userStore.remove()
    }


}