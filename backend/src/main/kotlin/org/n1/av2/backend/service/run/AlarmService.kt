package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.GameEvent
import org.n1.av2.backend.engine.TimedEventQueue
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service


class AlarmGameEvent(val runId: String, val nodeId: String): GameEvent
class PatrollerArriveGameEvent(val runId: String, val patrollerId: String, val nodeId: String): GameEvent

@Service
class AlarmService(
        val hackerPositionService: HackerPositionService,
        val timedEventQueue: TimedEventQueue,
        val stompService: StompService) {


    class ActionStartPatroller(val patrollerId: String, val nodeId: String, val appearTicks:Int = 20)
    fun processAlarm(event: AlarmGameEvent) {
        stompService.toRun(event.runId, ReduxActions.SERVER_COMPLETE_COUNTDOWN)

        val patrollerId = "patroller-1"

        val message = ActionStartPatroller(patrollerId, event.nodeId)
        stompService.toRun(event.runId, ReduxActions.SERVER_START_PATROLLER, message)

        val next = PatrollerArriveGameEvent(event.runId, patrollerId, event.nodeId)
        timedEventQueue.queueInTicks(20, next)
    }

    class ActionPatrollerLocksHacker(val patrollerId: String, val hackerId: String)
    fun processPatrollerArrive(event: PatrollerArriveGameEvent) {

        val hackers = hackerPositionService.hackersAt(event.nodeId, event.runId)
        hackers.forEach {hackerId ->
            val message = ActionPatrollerLocksHacker(event.patrollerId, hackerId)
            stompService.toRun(event.runId, ReduxActions.SERVER_PATROLLER_LOCKS_HACKER, message)
            hackerPositionService.lockHacker(hackerId)
        }
    }

}