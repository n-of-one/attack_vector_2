package org.n1.av2.run.terminal.outside

import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.ScheduledTask
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.run.entity.RunEntityService
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
    private val configService: ConfigService,
    private val runEntityService: RunEntityService,
    private val outsideTerminalHelper: OutsideTerminalHelper,
) {

    fun startAttack(hackerState: HackerStateRunning) {
        startAttack(hackerState, false)
    }

    fun startQuickAttack(hackerState: HackerStateRunning) {
        if (!configService.getAsBoolean(ConfigItem.DEV_HACKER_USE_DEV_COMMANDS)) {
            connectionService.replyTerminalReceive("QuickAttack is disabled.")
            return
        }
        startAttack(hackerState, true)
    }

    private fun startAttack(hackerState: HackerStateRunning, quick: Boolean) {
        if (!outsideTerminalHelper.verifyOutside(hackerState)) return
        if (!outsideTerminalHelper.verifySiteNotShutdown(hackerState.siteId)) return

        val run = runEntityService.getByRunId(hackerState.runId)
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
        if (hackerState.runId == null) {
            return // left hack during the attack sequence
        }

        syntaxHighlightingService.sendForInside()
        val startNodeId = hackerStateEntityService.startedRun(userId, runId)

        commandMoveService.moveArrive(startNodeId, userId, runId)
    }

}
