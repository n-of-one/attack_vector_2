package org.n1.av2.backend.service.run

import org.n1.av2.backend.model.db.run.HackerState
import org.n1.av2.backend.model.db.run.RunActivity
import org.n1.av2.backend.model.db.user.HackerIcon
import org.n1.av2.backend.service.user.UserService
import org.springframework.stereotype.Service

class HackerPresence(val userId: String,
                     val userName: String,
                     val icon: HackerIcon,
                     val nodeId: String?,
                     val targetNodeId: String?,
                     val activity: RunActivity,
                     val locked: Boolean)

@Service
class HackerService(
        private val hackerStateService: HackerStateService,
        private val userService: UserService) {

    fun getPresenceInRun(runId: String): List<HackerPresence> {
        val hackersInRun = hackerStateService.getHackersInRun(runId)

        return hackersInRun.map{ state -> toPresence(state) }
    }

    fun toPresence(state: HackerState): HackerPresence {
        val user = userService.getById(state.userId)

        return HackerPresence(
            userId = user.id,
            userName = user.name,
            icon = user.icon,
            nodeId = state.currentNodeId,
            targetNodeId = state.currentNodeId,
            activity = state.runActivity,
            locked = state.locked)
    }

}