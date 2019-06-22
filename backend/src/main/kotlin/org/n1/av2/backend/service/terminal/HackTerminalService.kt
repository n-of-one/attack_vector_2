package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.config.MyEnvironment
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class HackTerminalService(
        val stompService: StompService,
        val scanTerminalService: ScanTerminalService,
        val currentUserService: CurrentUserService,
        val environment: MyEnvironment) {

    fun processCommand(runId: String, command: String) {
        val tokens = command.split(" ")
        when (tokens[0]) {
            "help" -> processHelp()
            "mv" -> processMove(runId, tokens)
            "dc" -> processDisconnect()
            "hack" -> processHack(runId, tokens)
            "servererror" -> error("gah")
            "/share" -> processShare(runId, tokens)

            else -> stompService.terminalReceive("Unknown command, try [u]help[/].")
        }
    }

    private fun processHelp() {
        stompService.terminalReceive(
                "Command options:",
                " [u]mv [ok]<network id>[/]     -- for example: [u]mv[ok] 00",
                " [u]view",
                " [u]hack [primary]<layer>[/]        -- for example: [u]hack[primary] 00",
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

    private fun processHack(runId: String, tokens: List<String>) {
        stompService.terminalReceive("[warn]Not implemented yet, sorry");
    }

    private fun processMove(runId: String, tokens: List<String>) {
        stompService.terminalReceive("[warn]Not implemented yet, sorry");
    }

    private fun processDisconnect() {
        stompService.terminalReceive("[warn]Not implemented yet, sorry");
    }

    private fun processShare(runId: String, tokens: List<String>) {
        scanTerminalService.processShare(runId, tokens)
    }

}