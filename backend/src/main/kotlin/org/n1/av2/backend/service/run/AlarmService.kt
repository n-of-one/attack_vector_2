package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.GameEvent
import org.n1.av2.backend.engine.TimedEventQueue
import org.n1.av2.backend.model.db.layer.TimerTriggerLayer
import org.n1.av2.backend.model.db.run.HomingPatroller
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.HomingPatrollerRepo
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.scan.TraverseNodeService
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


class AlarmGameEvent(val runId: String, val nodeId: String, val userId: String): GameEvent
class PatrollerArriveGameEvent(val patrollerId: String, val nodeId: String, val runId: String): GameEvent


const val PATROLLER_ARRIVE_FIRST_TICKS = 20
const val PATROLLER_MOVE_TICKS = 15

@Service
class AlarmService(
        val hackerPositionService: HackerPositionService,
        val timedEventQueue: TimedEventQueue,
        val homingPatrollerRepo: HomingPatrollerRepo,
        val traverseNodeService: TraverseNodeService,
        val time: TimeService,
        val stompService: StompService) {


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


    class ActionStartPatroller(val patrollerId: String, val nodeId: String, val appearTicks:Int = PATROLLER_ARRIVE_FIRST_TICKS)
    fun processAlarm(event: AlarmGameEvent) {
        stompService.toRun(event.runId, ReduxActions.SERVER_COMPLETE_COUNTDOWN)

        val patroller = HomingPatroller(
                id = createId("homingPatroller", homingPatrollerRepo::findById),
                runId = event.runId,
                targetUserId = event.userId,
                siteId = hackerPositionService.retrieve(event.userId).siteId,
                currentNodeId = event.nodeId
        )
        homingPatrollerRepo.save(patroller)

        val action = ActionStartPatroller(patroller.id, event.nodeId)
        stompService.toRun(event.runId, ReduxActions.SERVER_START_PATROLLER, action)

        val next = PatrollerArriveGameEvent(patroller.id, event.nodeId, event.runId)
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
            stompService.terminalReceiveForUser( patroller.targetUserId, "[error]critical[/] OS privileges revoked.")
            // TODO start tracing
            // TODO schedule removal of patroller from view
        }
        else {
            movePatrollerLeashTowardsHacker(patroller, event.runId)
        }
    }

    class ActionPatrollerMove(val patrollerId: String, val fromNodeId: String, val toNodeId: String, val moveTicks: Int)
    private fun movePatrollerLeashTowardsHacker(patroller: HomingPatroller, runId: String) {
        val traverseNodesById = traverseNodeService.createTraverseNodesWithoutDistance(patroller.siteId)
        val startTraverseNode = traverseNodesById[patroller.currentNodeId]!!
        startTraverseNode.fillDistanceFromHere(0)
        val targetNodeId = hackerPositionService.retrieve(patroller.targetUserId).currentNodeId
        val targetTraverseNode = traverseNodesById[targetNodeId]!!
        val nextNodeToTarget = targetTraverseNode.traceToDistance(1) ?: error("Cannot find path to hacker at ${targetNodeId} from ${patroller.currentNodeId}")

        stompService.toRun(runId, ReduxActions.SERVER_PATROLLER_MOVE, ActionPatrollerMove(patroller.id, patroller.currentNodeId, nextNodeToTarget.id, PATROLLER_MOVE_TICKS))

        val next = PatrollerArriveGameEvent(patroller.id, nextNodeToTarget.id, runId)
        timedEventQueue.queueInTicks(PATROLLER_MOVE_TICKS + 1, next)
    }


    // TODO deal with hackers whose connection is reset while timer is running. They need to see the alarm timer.
    // TODO deal with hackers whose connection is reset while being hunted by the patroller. Either due to active DC or due to disconnect.
    // TODO deal with hackers logging into a run when there is an in progress patroller chasing / tracing someone.


}