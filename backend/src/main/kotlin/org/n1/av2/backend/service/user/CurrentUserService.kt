package org.n1.av2.backend.service.user

import org.n1.av2.backend.entity.user.SYSTEM_USER
import org.n1.av2.backend.entity.user.UserEntity
import org.n1.av2.backend.entity.user.UserType.ADMIN
import org.n1.av2.backend.model.iam.UserPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class CurrentUserService {
    private val userEntityStore = ThreadLocal<UserEntity>()

    private val logger = mu.KotlinLogging.logger {}

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

    val isAdmin: Boolean
        get() {
            val user = userEntityStore.get() ?: return false
            return user.type == ADMIN
        }

    fun remove() {
        userEntityStore.remove()
    }

    val connectionId: String
        get() {
            return (SecurityContextHolder.getContext().authentication as UserPrincipal).connectionId
        }
}