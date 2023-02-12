package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.run.IceStatusRepo
import org.n1.av2.backend.entity.run.LayerStatusRepo
import org.n1.av2.backend.entity.run.NodeStatusRepo
import org.n1.av2.backend.model.Ticks
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.n1.av2.backend.service.terminal.HackTerminalService
import org.n1.av2.backend.service.terminal.hacking.CommandMoveService
import org.n1.av2.backend.service.terminal.hacking.MoveArriveGameEvent
import org.springframework.stereotype.Service

private val START_ATTACK_SLOW = Ticks("main" to 250)
private val NO_TICKS = Ticks("main" to 0)
private val START_ATTACK_FAST = NO_TICKS


class StartAttackArriveGameEvent(val userId: String, val runId: String)

@Service
class StartAttackService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val currentUserService: CurrentUserService,
    private val hackTerminalService: HackTerminalService,
    private val layerStatusRepo: LayerStatusRepo,
    private val nodeStatusRepo: NodeStatusRepo,
    private val iceStatusRepo: IceStatusRepo,
    private val tracingPatrollerService: TracingPatrollerService,
    private val taskRunner: TaskRunner,
    private val commandMoveService: CommandMoveService,
    private val stompService: StompService) {

    private val logger = mu.KotlinLogging.logger {}

    fun startAttack(runId: String, quick: Boolean) {
        val userId = currentUserService.userId

        hackerStateEntityService.startRun(userId, runId)

        data class StartRun(val userId: String, val quick: Boolean, val ticks: Ticks)
        val ticks = if (quick) START_ATTACK_FAST else START_ATTACK_SLOW
        val data = StartRun(userId, quick, ticks)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_START_ATTACK, data)


        val next = StartAttackArriveGameEvent(userId, runId)
        taskRunner.queueInTicks(ticks.total) { startAttackArrive(next) }
    }

    fun startAttackArrive(event: StartAttackArriveGameEvent) {
        hackTerminalService.sendSyntaxHighlighting(event.userId)
        val state = hackerStateEntityService.startedRun(event.userId, event.runId)

        val arrive = MoveArriveGameEvent(state.currentNodeId, event.userId, event.runId )
        commandMoveService.moveArrive(arrive)
    }

    fun purgeAll() {
        layerStatusRepo.deleteAll()
        iceStatusRepo.deleteAll()
        hackerStateEntityService.purgeAll()
        tracingPatrollerService.purgeAll()
    }

    fun reset() {
        layerStatusRepo.deleteAll()
        nodeStatusRepo.deleteAll()
        iceStatusRepo.deleteAll()
        tracingPatrollerService.purgeAll()
    }
}