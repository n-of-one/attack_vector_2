package org.n1.av2.backend.model.hacker

import org.n1.av2.backend.model.db.user.User


enum class HackerActivityType {
    ONLINE,     // this is the -other- activity indicator. Not visible or useful to other players other than that this hacker is online.
    SCANNING,   // this hacker is on the scanning page
    HACKING     // this hacker is actively hacking
}

class HackerActivity(
        val user: User,
        val type: HackerActivityType,
        val runId: String
)