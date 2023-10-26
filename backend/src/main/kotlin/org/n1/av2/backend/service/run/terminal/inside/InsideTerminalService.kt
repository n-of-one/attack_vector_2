package org.n1.av2.backend.service.run.terminal.inside

import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.run.terminal.CommandScanService
import org.n1.av2.backend.service.run.terminal.SocialTerminalService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class InsideTerminalService(
    private val stompService: StompService,
    private val socialTerminalService: SocialTerminalService,
    private val commandHackService: CommandHackService,
    private val commandMoveService: CommandMoveService,
    private val commandViewService: CommandViewService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val runService: RunService,
    private val commandScanService: CommandScanService,
) {

    fun processCommand(runId: String, command: String) {
        val tokens = command.trim().split(" ")
        val commandAction = tokens[0].lowercase()


        when (commandAction) {
            "help" -> processHelp()
            "dc" -> processDc(runId)
            "servererror" -> error("gah")
            "/share" -> socialTerminalService.processShare(runId, tokens)
            else -> processPrivilegedCommand(runId, tokens, commandAction)
        }
    }

    private fun processPrivilegedCommand(runId: String, tokens: List<String>, commandAction: String) {
        val state = hackerStateEntityService.retrieveForCurrentUser().toRunState()
        if (!state.masked) return reportLocked()

        when (commandAction) {
            "move" -> commandMoveService.processCommand(runId, tokens, state)
            "hack" -> commandHackService.processHackCommand(runId, tokens, state)
            "qhack" -> commandHackService.processQuickHack(runId, tokens, state)
            "view" -> commandViewService.process(runId, state)
            "connect" -> commandHackService.processConnectCommand(runId, tokens, state)
            "scan" -> commandScanService.processScanFromInside(runId, tokens, state)
            "/share" -> socialTerminalService.processShare(runId, tokens)

            else -> stompService.replyTerminalReceive("Unknown command, try [u]help[/].")
        }
    }

    private fun processHelp() {
        stompService.replyTerminalReceive(
                "Command options:",
                " [u]move[/] [ok]<network id>[/]     -- for example: [u]mv[ok] 01",
                " [u]view",
                " [u]connect[/] [primary]<layer>[/]          -- for example: [u]connect[primary] 1",
                " [u]hack[/] [primary]<layer>[/]          -- for example: [u]hack[primary] 1",
                " [u]scan[/]",
                " [u]dc",
                " [u]/share[/] [info]<user name>")
    }

    fun processDc(runId: String) {
        val hackerState = hackerStateEntityService.retrieveForCurrentUser()
        runService.hackerDisconnect(hackerState, "Disconnected")
    }

    fun reportLocked() {
        stompService.replyTerminalReceive("[error]critical[/] OS refuses operation with error message [error]unauthorized[/].")
    }

}