package org.n1.av2.backend.service.run.terminal.inside

import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.run.terminal.CommandHelpService
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
    private val commandHelpService: CommandHelpService,
) {

    fun processCommand(runId: String, command: String) {
        val tokens = command.trim().split(" ")
        val commandAction = tokens[0].lowercase()


        when (commandAction) {
            "help" -> commandHelpService.processHelp(true, tokens)
            "dc" -> processDc(runId)
            "servererror" -> error("gah")
            "/share" -> socialTerminalService.processShare(runId, tokens)
            else -> processPrivilegedCommand(runId, tokens, commandAction)
        }
    }

    private fun processPrivilegedCommand(runId: String, tokens: List<String>, commandAction: String) {
        val state = hackerStateEntityService.retrieveForCurrentUser().toRunState()

        when (commandAction) {
            "move" -> commandMoveService.processCommand(runId, tokens, state)
            "hack" -> commandHackService.processHackCommand(runId, tokens, state)
            "qhack" -> commandHackService.processQuickHack(runId, tokens, state)
            "view" -> commandViewService.process(runId, state)
            "password" -> commandHackService.processPasswordCommand(runId, tokens, state)
            "scan" -> commandScanService.processScanFromInside(runId, tokens, state)
            "/share" -> socialTerminalService.processShare(runId, tokens)

            else -> stompService.replyTerminalReceive("Unknown command, try [b]help[/].")
        }
    }



    fun processDc(runId: String) {
        val hackerState = hackerStateEntityService.retrieveForCurrentUser()
        runService.hackerDisconnect(hackerState, "Disconnected")
    }

}