package org.n1.av2.backend.model.iam

import org.n1.av2.backend.model.db.user.User
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class UserPrincipal(val user: User): Authentication {

    var invalidated = false

    val userId: String
        get() = user.id

    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        return user.type.authorities.toMutableList()
    }

    override fun setAuthenticated(p0: Boolean) {
        error("not supported")
    }

    override fun getName(): String {
        return if (invalidated)
            "error"
        else
            userId
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

    fun invalidate() {
        invalidated = true
    }
}