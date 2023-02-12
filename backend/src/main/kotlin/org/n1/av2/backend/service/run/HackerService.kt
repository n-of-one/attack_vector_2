package org.n1.av2.backend.service.run

import org.n1.av2.backend.entity.run.HackerState
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.run.RunActivity
import org.n1.av2.backend.entity.user.HackerIcon
import org.n1.av2.backend.entity.user.UserEntityService
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
    private val hackerStateEntityService: HackerStateEntityService,
    private val userEntityService: UserEntityService
) {

    fun getPresenceInRun(runId: String): List<HackerPresence> {
        val hackersInRun = hackerStateEntityService.getHackersInRun(runId)

        return hackersInRun.map{ state -> toPresence(state) }
    }

    fun toPresence(state: HackerState): HackerPresence {
        val user = userEntityService.getById(state.userId)

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