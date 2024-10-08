package org.n1.av2.platform.iam.user

import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class CurrentUserService {
    private val userEntityStore = ThreadLocal<UserEntity>()

    fun set(userEntity: UserEntity) {
        userEntityStore.set(userEntity)
    }

    val userEntity: UserEntity
        get() {
            return userEntityStore.get()
        }

    val userId: String
        get() {
            val userId = userEntityStore.get().id
            if (userId == SYSTEM_USER.id) error("Tried to access the current user from a game event context.")
            return userId
        }

    val isSystemUser: Boolean
        get() {
            val user = userEntityStore.get() ?: return true
            return user.id == SYSTEM_USER.id
        }
    fun remove() {
        userEntityStore.remove()
    }

    val connectionId: String
        get() {
            return (SecurityContextHolder.getContext().authentication as UserPrincipal).connectionId
        }
}
