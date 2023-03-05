package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.terminal.hacking.CommandHackService
import org.n1.av2.backend.service.terminal.hacking.CommandMoveService
import org.n1.av2.backend.service.terminal.hacking.CommandViewService
import org.springframework.stereotype.Service

@Service
class HackTerminalService(
    private val stompService: StompService,
    private val socialTerminalService: SocialTerminalService,
    private val commandHackService: CommandHackService,
    private val commandMoveService: CommandMoveService,
    private val commandViewService: CommandViewService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val runService: RunService,
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
        if (state.hookPatrollerId != null) return reportLocked()

        when (commandAction) {
            "move" -> commandMoveService.processCommand(runId, tokens, state)
            "hack" -> commandHackService.process(runId, tokens, state)
            "view" -> commandViewService.process(runId, state)
            "/share" -> socialTerminalService.processShare(runId, tokens)

            else -> stompService.replyTerminalReceive("Unknown command, try [u]help[/].")
        }
    }

    private fun processHelp() {
        stompService.replyTerminalReceive(
                "Command options:",
                " [u]move [ok]<network id>[/]     -- for example: [u]mv[ok] 00",
                " [u]view",
                " [u]hack [primary]<layer>[/]        -- for example: [u]hack[primary] 0",
                " [u]dc",
                " [u]/share [info]<user name>")
//        if (environment.dev) {
//            stompService.terminalReceive(
//                    "",
//                    "[i]Available only during development and testing:[/]",
//                    " [u]quickscan"
//            )
//        }
    }

    fun processDc(runId: String) {
        val hackerState = hackerStateEntityService.retrieveForCurrentUser()
        runService.leaveRun(hackerState)
    }

    fun reportLocked() {
        stompService.replyTerminalReceive("[error]critical[/] OS refuses operation with error message [error]unauthorized[/].")
    }




}