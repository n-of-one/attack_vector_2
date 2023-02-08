package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.config.MyEnvironment
import org.n1.av2.backend.model.Syntax
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.HackerStateService
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
        private val hackerStateService: HackerStateService,
        private val environment: MyEnvironment) {

    fun processCommand(runId: String, command: String) {
        val tokens = command.trim().split(" ")
        val commandAction = tokens[0].lowercase()


        when (commandAction) {
            "help" -> processHelp()
            "dc" -> stompService.terminalReceiveCurrentUser("[warn]Not implemented. Yet...")
            "servererror" -> error("gah")
            "/share" -> socialTerminalService.processShare(runId, tokens)
            else -> processPrivilegedCommand(runId, tokens, commandAction)
        }
    }

    private fun processPrivilegedCommand(runId: String, tokens: List<String>, commandAction: String) {
        val state = hackerStateService.retrieveForCurrentUser().toRunState()
        if (state.hookPatrollerId != null) return reportLocked()

        when (commandAction) {
            "move" -> commandMoveService.processCommand(runId, tokens, state)
            "hack" -> commandHackService.process(runId, tokens, state)
            "view" -> commandViewService.process(runId, state)
            "/share" -> socialTerminalService.processShare(runId, tokens)

            else -> stompService.terminalReceiveCurrentUser("Unknown command, try [u]help[/].")
        }
    }

    private fun processHelp() {
        stompService.terminalReceiveCurrentUser(
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

    fun reportLocked() {
        stompService.terminalReceiveCurrentUser("[error]critical[/] OS refuses operation with error message [error]unauthorized[/].")
    }

    fun sendSyntaxHighlighting(userId: String) {
        val map = HashMap<String, Syntax>()

        map["help"] = Syntax("u", "error s")
        map["move"] = Syntax("u", "ok", "error s")
        map["view"] = Syntax("u", "error s")
        map["hack"] = Syntax("u", "primary", "error s")
        map["dc"] = Syntax("u", "error s")
        map["/share"] = Syntax("u warn", "info", "error s")

        sendSyntaxHighlighting(map, userId, stompService)
    }


}