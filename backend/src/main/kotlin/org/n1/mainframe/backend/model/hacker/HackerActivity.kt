package org.n1.mainframe.backend.model.hacker

import org.n1.mainframe.backend.model.iam.UserPrincipal

enum class HackerActivityType {
    ONLINE,     // this is the -other- activity indicator. Not visible or useful to other players other than that this hacker is online.
    SCANNING,   // this hacker is on the scanning page
    HACKING     // this hacker is actively hacking
}

class HackerActivity(
        val authentication: UserPrincipal,
        val type: HackerActivityType,
        val id: String
)