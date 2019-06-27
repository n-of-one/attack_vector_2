package org.n1.av2.backend.service.run

import mu.KLogging
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.ReduxActions
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class HackingService(
        val hackerPositionService: HackerPositionService,
        val currentUserService: CurrentUserService,
        val stompService: StompService
) {

    companion object : KLogging()

    private data class MoveArrive(val nodeId: String, val userId: String)
    fun moveArrive(nodeId: String, runId: String) {
        hackerPositionService.arriveAt(nodeId)
        val data = MoveArrive(nodeId, currentUserService.userId)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_MOVE_ARRIVE, data)
    }

}