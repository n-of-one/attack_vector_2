package org.n1.av2.backend.model.iam

import org.n1.av2.backend.entity.user.User
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

enum class ConnectionType {
    WS_GENERAL,
    WS_ICE,
    WEB_PAGE,
    INTERNAL // used for internal processes that don't really have a connection
}

data class UserPrincipal(
    private val _name: String,
    val connectionId: String,
    val user: User,
    val type: ConnectionType,
    val iceId: String? = null
): Authentication {

    /// Used in websocket connection
    override fun getName() = _name

    val userId: String
        get() = user.id

    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        return user.type.authorities.toMutableList()
    }

    override fun setAuthenticated(p0: Boolean) {
        error("not supported")
    }

    override fun getCredentials(): Any {
        error("not supported")
    }

    override fun getPrincipal(): Any {
        return user
    }

    override fun isAuthenticated(): Boolean {
        return true
    }

    override fun getDetails(): Any {
        return user
    }
}