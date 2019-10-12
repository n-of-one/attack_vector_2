package org.n1.av2.backend.model.db.user

import org.springframework.security.core.GrantedAuthority

val ROLE_USER = GrantedAuthority { "ROLE_USER" }
val ROLE_HACKER = GrantedAuthority { "ROLE_HACKER" }
val ROLE_SITE_MANAGER = GrantedAuthority { "ROLE_SITE_MANAGER" }
val ROLE_ADMIN = GrantedAuthority { "ROLE_ADMIN" }
val ROLE_USER_MANAGER = GrantedAuthority { "ROLE_USER_MANAGER" }
val ROLE_MISSION_MANAGER = GrantedAuthority { "ROLE_MISSION_MANAGER" }
val ROLE_HACKER_MANAGER = GrantedAuthority { "ROLE_HACKER_MANAGER" }
val ROLE_LOGS = GrantedAuthority { "ROLE_LOGS" }

enum class UserType(vararg authorizationsInput: GrantedAuthority) {

    // synthetic users that is nog logged in.
    NOT_LOGGED_IN(),
    // regular player
    HACKER(ROLE_USER, ROLE_HACKER),
    // Regular GM
    GM(ROLE_USER, ROLE_SITE_MANAGER, ROLE_USER_MANAGER, ROLE_MISSION_MANAGER, ROLE_USER_MANAGER, ROLE_HACKER_MANAGER, ROLE_LOGS),
    // For preparing system, backups, etc.
    ADMIN(ROLE_USER, ROLE_SITE_MANAGER, ROLE_USER_MANAGER, ROLE_MISSION_MANAGER, ROLE_USER_MANAGER, ROLE_HACKER_MANAGER, ROLE_LOGS,
            ROLE_ADMIN);

    val authorities = authorizationsInput.toList()
}