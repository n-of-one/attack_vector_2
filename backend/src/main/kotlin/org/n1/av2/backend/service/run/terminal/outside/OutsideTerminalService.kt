package org.n1.av2.backend.service.run.terminal.outside

import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.service.run.terminal.CommandHelpService
import org.n1.av2.backend.service.run.terminal.CommandScanService
import org.n1.av2.backend.service.run.terminal.SocialTerminalService
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.service.util.TimeService
import org.springframework.stereotype.Service

@Service
class OutsideTerminalService(
    private val socialTerminalService: SocialTerminalService,
    private val commandStartAttackService: CommandStartAttackService,
    private val stompService: StompService,
    private val runEntityService: RunEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val timeService: TimeService,
    private val commandScanService: CommandScanService,
    private val commandHelpService: CommandHelpService,
) {


    fun processCommand(runId: String, command: String) {
        val tokens = command.split(" ")
        when (tokens[0]) {
            "help" -> commandHelpService.processHelp(false, tokens)
            "/share" -> socialTerminalService.processShare(runId, tokens)
            "move", "view", "hack", "connect" -> reportHackCommand()
            "servererror" -> error("gah")
            else -> processActiveSiteCommand(runId, tokens)
        }
    }

    fun processActiveSiteCommand(runId: String, tokens: List<String>) {
        val run = runEntityService.getByRunId(runId)
        if (isSiteShutdown(run)) {
            stompService.replyTerminalReceive("Connection refused. (site is in shutdown mode)")
            return
        }

        when (tokens[0]) {
            "scan" ->  commandScanService.processScanFromOutside(run)
            "quickscan", "qs" -> commandScanService.processQuickScan(run)
            "attack" ->  processAttack(run, false)
            "quickattack", "qa" -> processAttack(run, true)
        else -> stompService.replyTerminalReceive("Unknown command, try [b]help[/].")
        }
    }

    private fun reportHackCommand() {
        stompService.replyTerminalReceive("[warn]still scanning[/] - First initiate the attack with: [b]attack[/]")
    }




    private fun processAttack(run: Run, quick: Boolean) {
        commandStartAttackService.startAttack(run, quick)
    }


    fun isSiteShutdown(run: Run): Boolean {
        val siteProperties = sitePropertiesEntityService.getBySiteId(run.siteId)
        val now = timeService.now()
        return (siteProperties.shutdownEnd != null && now < siteProperties.shutdownEnd)
    }

}