package org.n1.av2.backend.service.run

import mu.KLogging
import org.n1.av2.backend.engine.TicksGameEvent
import org.n1.av2.backend.engine.TimedEventQueue
import org.n1.av2.backend.model.Ticks
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.IceStatusRepo
import org.n1.av2.backend.repo.LayerStatusRepo
import org.n1.av2.backend.repo.NodeStatusRepo
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.n1.av2.backend.service.terminal.HackTerminalService
import org.n1.av2.backend.service.terminal.hacking.CommandMoveService
import org.n1.av2.backend.service.terminal.hacking.MoveArriveGameEvent
import org.n1.av2.backend.service.user.HackerActivityService
import org.springframework.stereotype.Service

private val START_ATTACK_SLOW = Ticks("total" to 250)
private val NO_TICKS = Ticks("total" to 0)
private val START_ATTACK_FAST = NO_TICKS

class StartAttackArriveGameEvent(val userId: String, val runId: String, ticks: Ticks) : TicksGameEvent(ticks)


@Service
class HackingService(
        private val hackerPositionService: HackerPositionService,
        private val currentUserService: CurrentUserService,
        private val userActivityService: HackerActivityService,
        private val hackTerminalService: HackTerminalService,
        private val layerStatusRepo: LayerStatusRepo,
        private val nodeStatusRepo: NodeStatusRepo,
        private val iceStatusRepo: IceStatusRepo,
        private val tracingPatrollerService: TracingPatrollerService,
        private val timedEventQueue: TimedEventQueue,
        private val commandMoveService: CommandMoveService,
        private val stompService: StompService) {

    companion object : KLogging()


    data class StartRun(val userId: String, val quick: Boolean)

    fun startAttack(runId: String, quick: Boolean) {
        val userId = currentUserService.userId

        val data = StartRun(userId, quick)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_START_ATTACK, data)

        val ticks = if (quick) START_ATTACK_FAST else START_ATTACK_SLOW

        val next = StartAttackArriveGameEvent(userId, runId, ticks)
        timedEventQueue.queueInTicks(next)
    }

    fun startAttackArrive(event: StartAttackArriveGameEvent) {
        userActivityService.startActivityHacking(event.userId, event.runId)
        hackTerminalService.sendSyntaxHighlighting(event.userId)
        val position = hackerPositionService.startRun(event.runId)

        val arrive = MoveArriveGameEvent(position.currentNodeId, event.userId, event.runId, NO_TICKS)
        commandMoveService.moveArrive(arrive)
    }


    fun purgeAll() {
        layerStatusRepo.deleteAll()
        iceStatusRepo.deleteAll()
        hackerPositionService.purgeAll()
        tracingPatrollerService.purgeAll()
    }

    fun reset() {
        layerStatusRepo.deleteAll()
        nodeStatusRepo.deleteAll()
        iceStatusRepo.deleteAll()
        tracingPatrollerService.purgeAll()
    }

}