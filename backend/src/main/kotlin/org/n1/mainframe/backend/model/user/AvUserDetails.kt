package org.n1.mainframe.backend.model.user

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


open class AvUserDetails(private val id: String,
                         private val username: String,
                         private val password: String,
                         private val authorities: MutableCollection<GrantedAuthority>) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
       return authorities
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    companion object {

        fun create(user: User): AvUserDetails {
            return AvUserDetails(
                    user.id,
                    user.userName,
                    user.encodedPasscoded,
                    user.type.authorities
            )
        }
    }
}