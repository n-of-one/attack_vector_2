package org.n1.mainframe.backend.model.user.enums

import org.springframework.security.core.GrantedAuthority

val ROLE_HACKER = GrantedAuthority { "ROLE_HACKER" }
val ROLE_SITE_MANAGER = GrantedAuthority { "ROLE_SITE_MANAGER" }
val ROLE_ADMIN = GrantedAuthority { "ROLE_ADMIN" }
val ROLE_USER_MANAGER = GrantedAuthority { "ROLE_USER_MANAGER" }
val ROLE_MISSION_MANAGER = GrantedAuthority { "ROLE_MISSION_MANAGER" }
val ROLE_HACKER_MANAGER = GrantedAuthority { "ROLE_HACKER_MANAGER" }

enum class UserType(vararg authorizationsInput: GrantedAuthority) {

    // synthetic users that is nog logged in.
    NOT_LOGGED_IN(),
    // regular player
    HACKER(ROLE_HACKER),
    // Player with additional rights such as managing other players.
    HACKER_MANAGER(ROLE_HACKER, ROLE_USER_MANAGER),
    // Regular GM
    GM(ROLE_SITE_MANAGER, ROLE_USER_MANAGER, ROLE_MISSION_MANAGER, ROLE_HACKER_MANAGER),
    // For preparing system, backups, etc.
    ADMIN(ROLE_ADMIN, ROLE_HACKER, ROLE_SITE_MANAGER, ROLE_USER_MANAGER, ROLE_MISSION_MANAGER, ROLE_HACKER_MANAGER);

    val authorities = authorizationsInput.toMutableList()
}