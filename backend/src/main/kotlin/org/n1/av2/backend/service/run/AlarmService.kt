package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.GameEvent
import org.n1.av2.backend.engine.TimedEventQueue
import org.n1.av2.backend.model.db.layer.TimerTriggerLayer
import org.n1.av2.backend.model.db.run.HomingPatroller
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.HomingPatrollerRepo
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


class AlarmGameEvent(val runId: String, val nodeId: String, val userId: String): GameEvent
class PatrollerArriveGameEvent(val runId: String, val patrollerId: String, val nodeId: String): GameEvent

@Service
class AlarmService(
        val hackerPositionService: HackerPositionService,
        val timedEventQueue: TimedEventQueue,
        val stompService: StompService,
        val homingPatrollerRepo: HomingPatrollerRepo,
        val time: TimeService) {


    fun hackerTriggers(layer: TimerTriggerLayer, nodeId: String, userId: String, runId: String) {

        class CountdownStart(val finishAt: ZonedDateTime)
        val alarmTime = time.now()
                .plusMinutes(layer.minutes)
                .plusSeconds(layer.seconds)
        stompService.toUser(ReduxActions.SERVER_START_COUNTDOWN, CountdownStart(alarmTime))

        val detectTime = format(layer.minutes, layer.seconds)

        stompService.terminalReceive("[pri]${layer.level}[/] Network sniffer : analyzing traffic. Persona mask will fail in [error]${detectTime}[/].")

        class FlashPatroller(val nodeId: String)
        stompService.toRun(runId, ReduxActions.SERVER_FLASH_PATROLLER, FlashPatroller(nodeId))

        timedEventQueue.queueInMinutesAndSeconds(layer.minutes, layer.seconds, AlarmGameEvent(runId, nodeId, userId))
    }

    private fun format(minutes: Long, seconds: Long): String {
        return "%02d:%02d".format(minutes, seconds)
    }


    class ActionStartPatroller(val patrollerId: String, val nodeId: String, val appearTicks:Int = 20)
    fun processAlarm(event: AlarmGameEvent) {
        stompService.toRun(event.runId, ReduxActions.SERVER_COMPLETE_COUNTDOWN)

        val patroller = HomingPatroller(
                id = createId("homingPatroller", homingPatrollerRepo::findById),
                runId = event.runId,
                targetUserId = event.userId,
                currentNodeId = event.nodeId
        )
        homingPatrollerRepo.save(patroller)

        val action = ActionStartPatroller(patroller.id, event.nodeId)
        stompService.toRun(event.runId, ReduxActions.SERVER_START_PATROLLER, action)

        val next = PatrollerArriveGameEvent(event.runId, patroller.id, event.nodeId)
        timedEventQueue.queueInTicks(20, next)
    }

    class ActionPatrollerLocksHacker(val patrollerId: String, val hackerId: String)
    fun processPatrollerArrive(event: PatrollerArriveGameEvent) {

        val patroller = homingPatrollerRepo.findById(event.patrollerId).get()
        patroller.currentNodeId = event.nodeId

        val targetPosition = hackerPositionService.retrieve(patroller.targetUserId)
        if (targetPosition.currentNodeId == patroller.currentNodeId) {
            val action = ActionPatrollerLocksHacker(event.patrollerId, patroller.targetUserId)
            stompService.toRun(event.runId, ReduxActions.SERVER_PATROLLER_LOCKS_HACKER, action)
            hackerPositionService.lockHacker(patroller.targetUserId)
            // TODO start tracing
            // TODO schedule removal of patroller from view
        }
        else {
            // TODO
        }

//        val hackers = hackerPositionService.hackersAt(event.nodeId, event.runId)
//        hackers.forEach {hackerId ->
//        }
    }


}