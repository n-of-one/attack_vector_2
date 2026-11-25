package org.n1.av2.run.terminal.outside

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.run.RunIngressService
import org.n1.av2.run.terminal.TERMINAL_MAIN
import org.n1.av2.script.ram.RamService
import org.n1.av2.site.SiteService
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service


@Service
class CommandStartAttackService(
    private val connectionService: ConnectionService,
    private val ramService: RamService,
    private val configService: ConfigService,
    private val outsideTerminalHelper: OutsideTerminalHelper,
    private val siteService: SiteService,
    private val nodeEntityService: NodeEntityService,
    private val runIngressService: RunIngressService,
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

    private fun startAttack(hackerState: HackerStateRunning, quickHacking: Boolean) {
        if (!outsideTerminalHelper.verifyOutside(hackerState)) return
        if (!outsideTerminalHelper.verifySiteNotShutdown(hackerState.siteId)) return

        val startNode = siteService.findStartNode(nodeEntityService.findBySiteId(hackerState.siteId))
            ?: return connectionService.replyTerminalReceive("[error]Network error[/] no route to this site.")

        connectionService.replyTerminalSetLocked(true)
        runIngressService.connect(hackerState.toState(), startNode.id, quickHacking, uplink = null)
        connectionService.reply(ServerActions.SERVER_TERMINAL_UPDATE_PROMPT, "prompt" to "⇋ ", "terminalId" to TERMINAL_MAIN)

        ramService.startHack(hackerState.userId)
    }
}
