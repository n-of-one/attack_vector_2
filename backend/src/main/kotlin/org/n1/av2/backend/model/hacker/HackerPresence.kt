package org.n1.av2.backend.model.hacker

import org.n1.av2.backend.model.db.user.HackerIcon

data class HackerPresence(val userId: String, val userName: String, val icon: HackerIcon)