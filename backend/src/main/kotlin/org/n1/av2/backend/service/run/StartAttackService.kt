package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.model.Timings
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.terminal.SyntaxHighlightingService
import org.n1.av2.backend.service.terminal.hacking.CommandMoveService
import org.n1.av2.backend.service.terminal.hacking.MoveArriveGameEvent
import org.springframework.stereotype.Service

private val START_ATTACK_SLOW = Timings("main" to 250)
private val NO_Timings = Timings("main" to 0)
private val START_ATTACK_FAST = NO_Timings


class StartAttackArriveGameEvent(val userId: String, val runId: String)

@Service
class StartAttackService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val currentUserService: CurrentUserService,
    private val taskRunner: TaskRunner,
    private val commandMoveService: CommandMoveService,
    private val syntaxHighlightingService: SyntaxHighlightingService,
    private val stompService: StompService
) {

    private val logger = mu.KotlinLogging.logger {}

    fun startAttack(runId: String, quick: Boolean) {
        val userId = currentUserService.userId

        hackerStateEntityService.startRun(userId, runId)

        data class StartRun(val userId: String, val quick: Boolean, val timings: Timings)
        val timings = if (quick) START_ATTACK_FAST else START_ATTACK_SLOW
        val data = StartRun(userId, quick, timings)
        stompService.toRun(runId, ServerActions.SERVER_HACKER_START_ATTACK, data)


        val next = StartAttackArriveGameEvent(userId, runId)
        taskRunner.queueInTicks(timings.totalTicks) { startAttackArrive(next) }
    }

    fun startAttackArrive(event: StartAttackArriveGameEvent) {
        syntaxHighlightingService.sendForAttack()
        val state = hackerStateEntityService.startedRun(event.userId, event.runId)

        val arrive = MoveArriveGameEvent(state.currentNodeId, event.userId, event.runId )
        commandMoveService.moveArrive(arrive)
    }

}