package org.n1.av2.run.terminal.outside

import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.ScheduledTask
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.run.entity.Run
import org.n1.av2.run.terminal.SyntaxHighlightingService
import org.n1.av2.run.terminal.TERMINAL_MAIN
import org.n1.av2.run.terminal.inside.CommandMoveService
import org.n1.av2.run.timings.Timings
import org.n1.av2.run.timings.TimingsService
import org.n1.av2.script.ram.RamService
import org.springframework.stereotype.Service


@Service
class CommandStartAttackService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val currentUserService: CurrentUserService,
    private val userTaskRunner: UserTaskRunner,
    private val commandMoveService: CommandMoveService,
    private val syntaxHighlightingService: SyntaxHighlightingService,
    private val connectionService: ConnectionService,
    private val timingsService: TimingsService,
    private val ramService: RamService,
) {

    fun startAttack(run: Run, quick: Boolean) {

        val userId = currentUserService.userId

        hackerStateEntityService.startAttack(userId, run)

        data class StartRun(val userId: String, val quick: Boolean, val timings: Timings)
        val timings = if (quick) timingsService.START_ATTACK_FAST else timingsService.START_ATTACK_SLOW
        val data = StartRun(userId, quick, timings)
        connectionService.replyTerminalSetLocked(true)
        connectionService.toRun(run.runId, ServerActions.SERVER_HACKER_START_ATTACK, data)
        connectionService.reply(ServerActions.SERVER_TERMINAL_UPDATE_PROMPT, "prompt" to "⇋ ", "terminalId" to TERMINAL_MAIN)

        userTaskRunner.queueInTicksForSite("attack-arrive", run.siteId, timings.totalTicks) { startAttackArrive(userId, run.runId) }

        ramService.startHack(userId)
    }

    @ScheduledTask
    fun startAttackArrive(userId: String, runId: String) {
        val hackerState = hackerStateEntityService.retrieve(userId)
        if (hackerState.runId == null ) {
            return // left hack during the attack sequence
        }

        syntaxHighlightingService.sendForInside()
        val state = hackerStateEntityService.startedRun(userId, runId)

        commandMoveService.moveArrive(state.currentNodeId, userId, runId)
    }

}
