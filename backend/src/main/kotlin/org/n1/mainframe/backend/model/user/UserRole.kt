package org.n1.mainframe.backend.model.user

import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * Holder class for user roles (Spring security Authority format)
 */
class UserRole {
 companion object {
     const val GM = "gm"
     const val HACKER = "hacker"
     const val ADMIN = "admin"

     val ROLE_HACKER = createRole(HACKER)
     val ROLE_GM = createRole(GM)
     val ROLE_ADMIN = createRole(ADMIN)

     private fun createRole(name: String): SimpleGrantedAuthority {
         return SimpleGrantedAuthority("ROLE_" + name)
     }

 }

}
