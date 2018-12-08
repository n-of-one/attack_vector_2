package org.n1.mainframe.backend.model

import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * Holder class for user roles (Spring security Authority format)
 */
class UserRole {
 companion object {
     const val GM = "gm"
     const val HACKER = "hacker"
     const val ADMIN = "admin"

     val AUTHORITY_ROLE_HACKER = createRole(HACKER)
     val AUTHORITY_ROLE_GM = createRole(GM)
     val AUTHORITY_ROLE_ADMIN = createRole(ADMIN)

     private fun createRole(name: String): SimpleGrantedAuthority {
         return SimpleGrantedAuthority("ROLE_" + name)
     }

 }

}
