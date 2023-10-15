package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.ScheduledTask
import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.SiteProperties
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.model.Timings
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.terminal.SyntaxHighlightingService
import org.n1.av2.backend.service.terminal.TERMINAL_MAIN
import org.n1.av2.backend.service.terminal.hacking.CommandMoveService
import org.n1.av2.backend.service.terminal.hacking.MoveArriveGameEvent
import org.springframework.stereotype.Service

private val START_ATTACK_SLOW = Timings("main" to 250)
private val NO_Timings = Timings("main" to 0)
private val START_ATTACK_FAST = NO_Timings


@Service
class StartAttackService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val currentUserService: CurrentUserService,
    private val userTaskRunner: UserTaskRunner,
    private val commandMoveService: CommandMoveService,
    private val syntaxHighlightingService: SyntaxHighlightingService,
    private val stompService: StompService,
    private val runEntityService: RunEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val timeService: TimeService,
) {

    private val logger = mu.KotlinLogging.logger {}

    fun startAttack(runId: String, quick: Boolean) {

        val run = runEntityService.getByRunId(runId)
        val siteProperties = sitePropertiesEntityService.getBySiteId(run.siteId)
        val now = timeService.now()

        if ( siteProperties.shutdownEnd != null && now < siteProperties.shutdownEnd ) {
            stompService.replyTerminalReceive("Connection refused. (site is in shutdown mode)")
            return
        }

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

        val arrive = MoveArriveGameEvent(state.currentNodeId, userId, runId )
        commandMoveService.moveArrive(arrive)
    }

}