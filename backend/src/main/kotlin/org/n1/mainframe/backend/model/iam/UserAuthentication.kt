package org.n1.mainframe.backend.model.iam

import org.n1.mainframe.backend.model.user.User
import org.n1.mainframe.backend.util.createId
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class UserAuthentication(val user: User): Authentication {

    var clientId: String? = null

    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        return user.type.authorities.toMutableList()
    }

    override fun setAuthenticated(p0: Boolean) {
        error("not supported")
    }

    override fun getName(): String {
        return if (clientId != null) "${clientId}:${user.userName}" else user.userName
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

    fun generateClientId() {
        if (this.clientId != null) {
            error("Client ID already set, cannot overwrite. Current value: ${this.clientId}")
        }
        this.clientId = createId("client" )
    }
}