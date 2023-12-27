package org.n1.av2.backend.service.run.terminal.outside

import org.n1.av2.backend.engine.ScheduledTask
import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.model.Timings
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.run.TERMINAL_MAIN
import org.n1.av2.backend.service.run.terminal.SyntaxHighlightingService
import org.n1.av2.backend.service.run.terminal.inside.CommandMoveService
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

private val START_ATTACK_FAST = Timings("main" to 0)
private val START_ATTACK_SLOW = Timings("main" to 190)

@Service
class CommandStartAttackService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val currentUserService: CurrentUserService,
    private val userTaskRunner: UserTaskRunner,
    private val commandMoveService: CommandMoveService,
    private val syntaxHighlightingService: SyntaxHighlightingService,
    private val stompService: StompService,
) {

    private val logger = mu.KotlinLogging.logger {}

    fun startAttack(run: Run, quick: Boolean) {

        val userId = currentUserService.userId

        hackerStateEntityService.startAttack(userId, run)

        data class StartRun(val userId: String, val quick: Boolean, val timings: Timings)
        val timings = if (quick) START_ATTACK_FAST else START_ATTACK_SLOW
        val data = StartRun(userId, quick, timings)
        stompService.replyTerminalSetLocked(true)
        stompService.toRun(run.runId, ServerActions.SERVER_HACKER_START_ATTACK, data)
        stompService.reply(ServerActions.SERVER_TERMINAL_UPDATE_PROMPT, "prompt" to "⇋ ", "terminalId" to TERMINAL_MAIN)

        userTaskRunner.queueInTicksForSite("attack-arrive", run.siteId, timings.totalTicks) { startAttackArrive(userId, run.runId) }
    }

    @ScheduledTask
    fun startAttackArrive(userId: String, runId: String) {
        syntaxHighlightingService.sendForInside()
        val state = hackerStateEntityService.startedRun(userId, runId)

        commandMoveService.moveArrive(state.currentNodeId, userId, runId)
    }

}