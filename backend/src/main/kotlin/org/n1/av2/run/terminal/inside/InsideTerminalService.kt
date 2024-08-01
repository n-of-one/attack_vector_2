package org.n1.av2.run.terminal.inside

import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.run.RunService
import org.n1.av2.run.terminal.CommandHelpService
import org.n1.av2.run.terminal.CommandScanService
import org.n1.av2.run.terminal.SocialTerminalService
import org.springframework.stereotype.Service

@Service
class InsideTerminalService(
    private val connectionService: ConnectionService,
    private val socialTerminalService: SocialTerminalService,
    private val commandHackService: CommandHackService,
    private val commandMoveService: CommandMoveService,
    private val commandViewService: CommandViewService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val runService: RunService,
    private val commandScanService: CommandScanService,
    private val commandHelpService: CommandHelpService,
    private val commandDebugService: CommandDebugService,
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

            "sweeperunblock" -> commandDebugService.processSweepUnblock(runId, tokens, state)


            else -> connectionService.replyTerminalReceive("Unknown command, try [b]help[/].")
        }
    }



    fun processDc(runId: String) {
        val hackerState = hackerStateEntityService.retrieveForCurrentUser()
        runService.hackerDisconnect(hackerState, "Disconnected")
    }

}
