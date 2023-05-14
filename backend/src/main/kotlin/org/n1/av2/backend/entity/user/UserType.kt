package org.n1.av2.backend.entity.user

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
val ROLE_USER_MANAGER = AvAuthority ( "ROLE_USER_MANAGER" )      // can create and edit usersr
val ROLE_HACKER_MANAGER = AvAuthority ( "ROLE_HACKER_MANAGER" )    // can create/edit hackers, a lesser version of user manager, cannot create gms or admins
val ROLE_MISSION_MANAGER = AvAuthority ( "ROLE_MISSION_MANAGER" )  // can create missions
val ROLE_LOGS = AvAuthority ( "ROLE_LOGS" )        // can view logs

enum class UserType(vararg authorizationsInput: GrantedAuthority) {

    // synthetic users that is nog logged in.
    NOT_LOGGED_IN(),

    // regular player
    HACKER(ROLE_USER, ROLE_HACKER),

    // player manager
    HACKER_MANAGER(ROLE_USER, ROLE_HACKER_MANAGER, ROLE_HACKER),

    // Regular GM
    GM(ROLE_USER, ROLE_SITE_MANAGER, ROLE_USER_MANAGER, ROLE_MISSION_MANAGER, ROLE_LOGS),
    // For preparing system, backups, etc.
    ADMIN(
        ROLE_USER, ROLE_SITE_MANAGER, ROLE_USER_MANAGER, ROLE_MISSION_MANAGER, ROLE_LOGS,
            ROLE_ADMIN
    );

    val authorities = authorizationsInput.toList()
}