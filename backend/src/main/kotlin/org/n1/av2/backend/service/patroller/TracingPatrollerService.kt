package org.n1.av2.backend.service.patroller

import org.n1.av2.backend.engine.GameEvent
import org.n1.av2.backend.engine.TimedEventQueue
import org.n1.av2.backend.model.db.run.TracingPatroller
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.TracingPatrollerRepo
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.layer.PATROLLER_ARRIVE_FIRST_TICKS
import org.n1.av2.backend.service.layer.PATROLLER_MOVE_TICKS
import org.n1.av2.backend.service.run.HackerPositionService
import org.n1.av2.backend.service.scan.TraverseNode
import org.n1.av2.backend.service.scan.TraverseNodeService
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service


class TracingPatrollerArrivesGameEvent(val patrollerId: String, val nodeId: String): GameEvent


@Service
class TracingPatrollerService(
        val hackerPositionService: HackerPositionService,
        val timedEventQueue: TimedEventQueue,
        val tracingPatrollerRepo: TracingPatrollerRepo,
        val traverseNodeService: TraverseNodeService,
        val time: TimeService,
        val stompService: StompService) {


    fun activatePatroller(nodeId: String, userId: String, runId: String) {
        val patroller = TracingPatroller(
                id = createId("tracingPatroller", tracingPatrollerRepo::findById),
                runId = runId,
                targetUserId = userId,
                siteId = hackerPositionService.retrieve(userId).siteId,
                currentNodeId = nodeId,
                originatingNodeId = nodeId
        )
        tracingPatrollerRepo.save(patroller)

        messageStartPatroller(patroller, nodeId, runId)

        val next = TracingPatrollerArrivesGameEvent(patroller.id, nodeId)
        timedEventQueue.queueInTicks(PATROLLER_ARRIVE_FIRST_TICKS, next)
    }

    fun patrollerArrives(event: TracingPatrollerArrivesGameEvent) {
        val patroller = tracingPatrollerRepo.findById(event.patrollerId).get()
        patroller.currentNodeId = event.nodeId

        val targetPosition = hackerPositionService.retrieve(patroller.targetUserId)
        if (targetPosition.currentNodeId == patroller.currentNodeId) {
            messageLockHacker(patroller)
            hackerPositionService.lockHacker(patroller.targetUserId)
            stompService.terminalReceiveForUser( patroller.targetUserId, "[error]critical[/] OS privileges revoked.")

            // TODO start tracing
            // TODO schedule removal of patroller from view
        }
        else {
            patrollerMove(patroller, patroller.runId)
        }
    }

    private fun patrollerMove(patroller: TracingPatroller, runId: String) {
        val nextNodeToTarget = findMoveNextNode(patroller)
        messagePatrollerMove(patroller, nextNodeToTarget.id, runId)

        val next = TracingPatrollerArrivesGameEvent(patroller.id, nextNodeToTarget.id)
        timedEventQueue.queueInTicks(PATROLLER_MOVE_TICKS + 1, next)
    }


    private fun findMoveNextNode(patroller: TracingPatroller): TraverseNode {
        val traverseNodesById = traverseNodeService.createTraverseNodesWithoutDistance(patroller.siteId)
        val startTraverseNode = traverseNodesById[patroller.currentNodeId]!!
        startTraverseNode.fillDistanceFromHere(0)
        val targetNodeId = hackerPositionService.retrieve(patroller.targetUserId).currentNodeId
        val targetTraverseNode = traverseNodesById[targetNodeId]!!
        return targetTraverseNode.traceToDistance(1) ?: error("Cannot find path to hacker at ${targetNodeId} from ${patroller.currentNodeId}")
    }


    fun purgeAll() {
        tracingPatrollerRepo.deleteAll()
    }


    // TODO deal with hackers whose connection is reset while timer is running. They need to see the alarm timer when they return.
    // TODO deal with hackers whose connection is reset while being hunted by the patroller. Either due to active DC or due to disconnect.
    // TODO show patroller to hackers logging into a run when there is an in progress patroller chasing / tracing someone.




    // -- Messages to UI -- //

    private fun messageStartPatroller(patroller: TracingPatroller, nodeId: String, runId: String) {
        class ActionStartPatroller(val patrollerId: String, val nodeId: String, val appearTicks:Int = PATROLLER_ARRIVE_FIRST_TICKS)
        val action = ActionStartPatroller(patroller.id, nodeId)
        stompService.toRun(runId, ReduxActions.SERVER_START_TRACING_PATROLLER, action)
    }

    private fun messageLockHacker(patroller: TracingPatroller) {
        class ActionPatrollerLocksHacker(val patrollerId: String, val hackerId: String)
        val action = ActionPatrollerLocksHacker(patroller.id, patroller.targetUserId)
        stompService.toRun(patroller.runId, ReduxActions.SERVER_PATROLLER_LOCKS_HACKER, action)
    }

    private fun messagePatrollerMove(patroller: TracingPatroller, nextNodeId: String, runId: String) {
        class ActionPatrollerMove(val patrollerId: String, val fromNodeId: String, val toNodeId: String, val moveTicks: Int)
        val action = ActionPatrollerMove(patroller.id, patroller.currentNodeId, nextNodeId, PATROLLER_MOVE_TICKS)
        stompService.toRun(runId, ReduxActions.SERVER_PATROLLER_MOVE, action)
    }

}