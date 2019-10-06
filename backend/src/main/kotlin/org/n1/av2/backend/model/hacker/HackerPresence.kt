package org.n1.av2.backend.model.hacker

import org.n1.av2.backend.model.db.user.HackerIcon

open class HackerPresence(val userId: String,
                          val userName: String,
                          val icon: HackerIcon,
                          val hacking: Boolean = false)

class HackerPresenceHacking(userId: String,
                            userName: String,
                            icon: HackerIcon,
                            val nodeId: String,
                            val targetNodeId: String?,
                            val inTransit: Boolean,
                            val locked: Boolean) : HackerPresence(userId, userName, icon, true)