package org.n1.av2.backend.service.run.outside

import org.n1.av2.backend.engine.ScheduledTask
import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.model.Timings
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.run.TERMINAL_MAIN
import org.n1.av2.backend.service.run.inside.command.CommandMoveService
import org.n1.av2.backend.service.run.inside.command.MoveArriveGameEvent
import org.n1.av2.backend.service.run.terminal.SyntaxHighlightingService
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

private val START_ATTACK_FAST = Timings("main" to 0)
private val START_ATTACK_SLOW = Timings("main" to 250)

@Service
class StartAttackService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val currentUserService: CurrentUserService,
    private val userTaskRunner: UserTaskRunner,
    private val commandMoveService: CommandMoveService,
    private val syntaxHighlightingService: SyntaxHighlightingService,
    private val stompService: StompService,
) {

    private val logger = mu.KotlinLogging.logger {}

    fun startAttack(runId: String, quick: Boolean) {

        val userId = currentUserService.userId

        hackerStateEntityService.startRun(userId, runId)

        data class StartRun(val userId: String, val quick: Boolean, val timings: Timings)
        val timings = if (quick) START_ATTACK_FAST else START_ATTACK_SLOW
        val data = StartRun(userId, quick, timings)
        stompService.replyTerminalSetLocked(true)
        stompService.toRun(runId, ServerActions.SERVER_HACKER_START_ATTACK, data)
        stompService.reply(ServerActions.SERVER_TERMINAL_UPDATE_PROMPT, "prompt" to "⇋ ", "terminalId" to TERMINAL_MAIN)


        userTaskRunner.queueInTicks(timings.totalTicks) { startAttackArrive(userId, runId) }
    }

    @ScheduledTask
    fun startAttackArrive(userId: String, runId: String) {
        syntaxHighlightingService.sendForAttack()
        val state = hackerStateEntityService.startedRun(userId, runId)

        val arrive = MoveArriveGameEvent(state.currentNodeId, userId, runId)
        commandMoveService.moveArrive(arrive)
    }

}