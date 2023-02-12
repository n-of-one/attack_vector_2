package org.n1.av2.backend.service.patroller

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.engine.TicksGameEvent
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.run.PatrollerPathSegment
import org.n1.av2.backend.entity.run.TracingPatroller
import org.n1.av2.backend.entity.run.TracingPatrollerRepo
import org.n1.av2.backend.model.Ticks
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.scan.TraverseNode
import org.n1.av2.backend.service.scan.TraverseNodeService
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct


val PATROLLER_ARRIVE_FIRST_TICKS = Ticks("appear" to 20)
val PATROLLER_MOVE_TICKS = Ticks("move" to 15)


class TracingPatrollerArrivesGameEvent(val patrollerId: String, val nodeId: String, ticks: Ticks) : TicksGameEvent(ticks)

class PatrollerUiData(val patrollerId: String, val nodeId: String, val path: List<PatrollerPathSegment>, val ticks: Ticks = PATROLLER_ARRIVE_FIRST_TICKS)


@Service
class TracingPatrollerService(
    val hackerStateEntityService: HackerStateEntityService,
    val taskRunner: TaskRunner,
    val tracingPatrollerRepo: TracingPatrollerRepo,
    val traverseNodeService: TraverseNodeService,
    val time: TimeService,
    val stompService: StompService) {

    @PostConstruct
    fun init() {
        // After a reboot, all users are logged out by default, so all remaining patrollers need to be removed.
        tracingPatrollerRepo.deleteAll()
    }

    private fun getPatroller(patrollerId: String): TracingPatroller {
        return tracingPatrollerRepo.findById(patrollerId).orElseThrow { RuntimeException("Patroller ${patrollerId} not found.") }
    }

    fun getAllForRun(runId: String): List<PatrollerUiData> {
        return tracingPatrollerRepo
                .findAllByRunId(runId)
                .map { patroller ->
                    PatrollerUiData(patroller.id, patroller.originatingNodeId, patroller.path)
                }
    }

    fun activatePatroller(nodeId: String, userId: String, runId: String) {
        val patroller = TracingPatroller(
                id = createId("tracingPatroller", tracingPatrollerRepo::findById),
                runId = runId,
                targetUserId = userId,
                siteId = hackerStateEntityService.retrieve(userId).toRunState().siteId,
                currentNodeId = nodeId,
                originatingNodeId = nodeId,
                path = ArrayList<PatrollerPathSegment>(0)
        )
        tracingPatrollerRepo.save(patroller)

        messageStartPatroller(patroller, nodeId, runId)

        val next = TracingPatrollerArrivesGameEvent(patroller.id, nodeId, PATROLLER_ARRIVE_FIRST_TICKS)
        taskRunner.queueInTicks(PATROLLER_ARRIVE_FIRST_TICKS.total) { patrollerArrives(next) }
    }

    fun patrollerArrives(event: TracingPatrollerArrivesGameEvent) {
        val patroller = getPatroller(event.patrollerId)
        patroller.currentNodeId = event.nodeId

        val targetHackerState = hackerStateEntityService.retrieve(patroller.targetUserId)
        if (targetHackerState.currentNodeId == patroller.currentNodeId) {
            lockHacker(patroller)
        } else {
            patrollerMove(patroller, patroller.runId)
        }
    }

    private fun patrollerMove(patroller: TracingPatroller, runId: String) {
        val nextNodeToTarget = findMoveNextNode(patroller)

        val segment = PatrollerPathSegment(patroller.currentNodeId, nextNodeToTarget.id)
        patroller.path.add(segment)
        tracingPatrollerRepo.save(patroller)

        messagePatrollerMove(patroller, segment, runId)

        val next = TracingPatrollerArrivesGameEvent(patroller.id, nextNodeToTarget.id, PATROLLER_MOVE_TICKS)
        taskRunner.queueInTicks(PATROLLER_MOVE_TICKS.total) { patrollerArrives(next) }
    }

    private fun lockHacker(patroller: TracingPatroller) {
        hackerStateEntityService.lockHacker(patroller.targetUserId, patroller.id)
        messageCatchHacker(patroller)
        stompService.terminalReceiveForUser(patroller.targetUserId, "[error]critical[/] OS privileges revoked.")

        // TODO start tracing
    }

    private fun findMoveNextNode(patroller: TracingPatroller): TraverseNode {
        val traverseNodesById = traverseNodeService.createTraverseNodesWithoutDistance(patroller.siteId)
        val startTraverseNode = traverseNodesById[patroller.currentNodeId]!!
        startTraverseNode.fillDistanceFromHere(0)
        val targetNodeId = hackerStateEntityService.retrieve(patroller.targetUserId).toRunState().currentNodeId
        val targetTraverseNode = traverseNodesById[targetNodeId]!!
        return targetTraverseNode.traceToDistance(1) ?: error("Cannot find path to hacker at ${targetNodeId} from ${patroller.currentNodeId}")
    }

    fun purgeAll() {
        tracingPatrollerRepo.deleteAll()
    }

    fun disconnected(userId: String) {
        tracingPatrollerRepo
                .findAllByTargetUserId(userId)
                .forEach { remove(it) }
    }

    private fun remove(patroller: TracingPatroller) {
        tracingPatrollerRepo.delete(patroller)

//      TODO: re-implement, rethink
//        timedEventQueue.removeAllFor(patroller.id)
        messageRemovePatroller(patroller.id, patroller.runId)
    }


    // -- Messages to UI -- //

    private fun messageStartPatroller(patroller: TracingPatroller, nodeId: String, runId: String) {
        class ActionStartPatroller(val patrollerId: String, val nodeId: String, val ticks: Ticks)

        val action = ActionStartPatroller(patroller.id, nodeId, PATROLLER_ARRIVE_FIRST_TICKS)
        stompService.toRun(runId, ReduxActions.SERVER_START_TRACING_PATROLLER, action)
    }

    private fun messageCatchHacker(patroller: TracingPatroller) {
        class ActionPatrollerCatchesHacker(val patrollerId: String, val hackerId: String)

        val action = ActionPatrollerCatchesHacker(patroller.id, patroller.targetUserId)
        stompService.toRun(patroller.runId, ReduxActions.SERVER_PATROLLER_LOCKS_HACKER, action)
    }

    private fun messagePatrollerMove(patroller: TracingPatroller, segment: PatrollerPathSegment, runId: String) {
        class ActionPatrollerMove(val patrollerId: String, val fromNodeId: String, val toNodeId: String, val ticks: Ticks)

        val action = ActionPatrollerMove(patroller.id, segment.fromNodeId, segment.toNodeId, PATROLLER_MOVE_TICKS)
        stompService.toRun(runId, ReduxActions.SERVER_PATROLLER_MOVE, action)
    }

    private fun messageRemovePatroller(patrollerId: String, runId: String) {
        class RemovePatroller(val patrollerId: String)

        val action = RemovePatroller(patrollerId)
        stompService.toRun(runId, ReduxActions.SERVER_PATROLLER_REMOVE, action)
    }


}