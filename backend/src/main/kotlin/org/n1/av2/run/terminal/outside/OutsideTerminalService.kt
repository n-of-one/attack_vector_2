package org.n1.av2.run.terminal.outside

import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.util.TimeService
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.terminal.CommandHelpService
import org.n1.av2.run.terminal.CommandScanService
import org.n1.av2.run.terminal.SocialTerminalService
import org.n1.av2.run.terminal.UNKNOWN_COMMAND_RESPONSE
import org.n1.av2.run.timings.TimingsService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service


const val CANNOT_SCAN_DURING_SITE_RESET = "Connection refused. (site is in shutdown mode)"

@Service
class OutsideTerminalService(
    private val socialTerminalService: SocialTerminalService,
    private val commandStartAttackService: CommandStartAttackService,
    private val connectionService: ConnectionService,
    private val runEntityService: RunEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val timeService: TimeService,
    private val commandScanService: CommandScanService,
    private val commandHelpService: CommandHelpService,
    private val timingsService: TimingsService,
    private val configService: ConfigService,
) {


    fun processCommand(runId: String, commandAction: String, tokens: List<String>) {
        when (commandAction) {
            "help" -> commandHelpService.processHelp(false, tokens)
            "/share" -> socialTerminalService.processShare(runId, tokens)
            "move", "view", "hack", "connect" -> reportHackCommand()
            "servererror" -> error("gah")
            "quick"-> {
                timingsService.minimize()
                connectionService.replyTerminalReceive("timings set to quick)")
            }
            else -> processActiveSiteCommand(runId, tokens)
        }
    }

    fun processActiveSiteCommand(runId: String, tokens: List<String>) {
        val run = runEntityService.getByRunId(runId)
        if (isSiteShutdown(run)) {
            connectionService.replyTerminalReceive(CANNOT_SCAN_DURING_SITE_RESET)
            return
        }

        when (tokens[0]) {
            "scan" ->  commandScanService.processScanFromOutside(run)
            "quickscan", "qs" -> commandScanService.processQuickScan(run)
            "attack" ->  processAttack(run, false)
            "quickattack", "qa" -> processAttack(run, true)
        else -> connectionService.replyTerminalReceive(UNKNOWN_COMMAND_RESPONSE)
        }
    }

    private fun reportHackCommand() {
        connectionService.replyTerminalReceive("[warn]You are outside[/] - First, start the attack with: [b]attack[/]")
    }

    private fun processAttack(run: Run, quick: Boolean) {
        if (quick && !configService.getAsBoolean(ConfigItem.DEV_HACKER_USE_DEV_COMMANDS)) {
            connectionService.replyTerminalReceive("QuickAttack is disabled.")
            return
        }
        commandStartAttackService.startAttack(run, quick)
    }


    fun isSiteShutdown(run: Run): Boolean {
        val siteProperties = sitePropertiesEntityService.getBySiteId(run.siteId)
        val now = timeService.now()
        return (siteProperties.shutdownEnd != null && now < siteProperties.shutdownEnd)
    }

}
