package org.n1.av2.backend.model.iam

import org.n1.av2.backend.config.websocket.ConnectionType
import org.n1.av2.backend.entity.user.SYSTEM_USER
import org.n1.av2.backend.entity.user.UserEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder


data class UserPrincipal(
    private val _name: String,
    val connectionId: String,
    val userEntity: UserEntity,
    val type: ConnectionType,
): Authentication {

    companion object {
        fun fromContext(): UserPrincipal {
            return SecurityContextHolder.getContext().authentication as UserPrincipal
        }
        fun system(): UserPrincipal {
            return UserPrincipal(
                _name = "system",
                connectionId = "none",
                userEntity = SYSTEM_USER,
                type = ConnectionType.NONE,
            )
        }
    }

    /// Used in websocket connection
    override fun getName() = _name

    val userId: String
        get() = userEntity.id

    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        return userEntity.type.authorities.toMutableList()
    }

    override fun setAuthenticated(p0: Boolean) {
        error("not supported")
    }

    override fun getCredentials(): Any {
        error("not supported")
    }

    override fun getPrincipal(): Any {
        return userEntity
    }

    override fun isAuthenticated(): Boolean {
        return true
    }

    override fun getDetails(): Any {
        return userEntity
    }
}