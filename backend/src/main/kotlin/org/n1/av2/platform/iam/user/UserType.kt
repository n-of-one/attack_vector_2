package org.n1.av2.platform.iam.user

import org.springframework.security.core.GrantedAuthority

class AvAuthority(private val authority: String) : GrantedAuthority {
    override fun getAuthority(): String {
        return authority
    }
}

val ROLE_USER = AvAuthority( "ROLE_USER" )        // a user, can log in
val ROLE_HACKER = AvAuthority ( "ROLE_HACKER" )    // can do hacking
val ROLE_SITE_MANAGER = AvAuthority ( "ROLE_SITE_MANAGER" ) // can create and manage sites
val ROLE_ADMIN = AvAuthority ( "ROLE_ADMIN" )                   // can do admin stuff like backup & restore
val ROLE_USER_MANAGER = AvAuthority ( "ROLE_USER_MANAGER" )      // can create and edit user
val ROLE_MISSION_MANAGER = AvAuthority ( "ROLE_MISSION_MANAGER" )  // can create missions
val ROLE_GM = AvAuthority ( "ROLE_GM" )  // can see GM only data

const val TEMPLATE_USER_NAME = "template"

enum class UserType(vararg authorizationsInput: GrantedAuthority) {

    // synthetic users that is nog logged in.
    NOT_LOGGED_IN,

    // regular player
    HACKER(ROLE_USER, ROLE_HACKER),

    // Regular GM
    GM(ROLE_USER, ROLE_SITE_MANAGER, ROLE_USER_MANAGER, ROLE_MISSION_MANAGER, ROLE_GM),

    // For preparing system, backups, etc.
    ADMIN(ROLE_USER, ROLE_USER_MANAGER, ROLE_MISSION_MANAGER, ROLE_ADMIN),

    SYSTEM;

    val authorities = authorizationsInput.toList()
}
