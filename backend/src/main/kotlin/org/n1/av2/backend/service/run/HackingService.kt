package org.n1.av2.backend.service.run

import mu.KLogging
import org.n1.av2.backend.engine.TaskRunner
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
import org.springframework.stereotype.Service

private val START_ATTACK_SLOW = Ticks("total" to 250)
private val NO_TICKS = Ticks("total" to 0)
private val START_ATTACK_FAST = NO_TICKS


class StartAttackArriveGameEvent(val userId: String, val runId: String)

@Service
class HackingService(
    private val hackerStateService: HackerStateService,
    private val currentUserService: CurrentUserService,
    private val hackTerminalService: HackTerminalService,
    private val layerStatusRepo: LayerStatusRepo,
    private val nodeStatusRepo: NodeStatusRepo,
    private val iceStatusRepo: IceStatusRepo,
    private val tracingPatrollerService: TracingPatrollerService,
    private val taskRunner: TaskRunner,
    private val commandMoveService: CommandMoveService,
    private val stompService: StompService) {

    companion object : KLogging()



    fun startAttack(runId: String, quick: Boolean) {
        val userId = currentUserService.userId

        hackerStateService.startRun(userId, runId)

        data class StartRun(val userId: String, val quick: Boolean)
        val data = StartRun(userId, quick)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_START_ATTACK, data)

        val ticks = if (quick) START_ATTACK_FAST else START_ATTACK_SLOW

        val next = StartAttackArriveGameEvent(userId, runId)
        taskRunner.queueInTicks(ticks.total) { startAttackArrive(next) }
    }

    fun startAttackArrive(event: StartAttackArriveGameEvent) {
        hackTerminalService.sendSyntaxHighlighting(event.userId)
        val state = hackerStateService.startedRun(event.userId, event.runId)

        val arrive = MoveArriveGameEvent(state.currentNodeId, event.userId, event.runId )
        commandMoveService.moveArrive(arrive)
        stompService.terminalSetLockedCurrentUser(false)
    }

    fun purgeAll() {
        layerStatusRepo.deleteAll()
        iceStatusRepo.deleteAll()
        hackerStateService.purgeAll()
        tracingPatrollerService.purgeAll()
    }

    fun reset() {
        layerStatusRepo.deleteAll()
        nodeStatusRepo.deleteAll()
        iceStatusRepo.deleteAll()
        tracingPatrollerService.purgeAll()
    }
}