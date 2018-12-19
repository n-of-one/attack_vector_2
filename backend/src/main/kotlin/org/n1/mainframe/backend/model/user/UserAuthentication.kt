package org.n1.mainframe.backend.model.user

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class UserAuthentication(val user: User): Authentication {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return user.type.authorities
    }

    override fun setAuthenticated(p0: Boolean) {
        error("not supported")
    }

    override fun getName(): String {
        return user.handle
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