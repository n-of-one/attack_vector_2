package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.config.MyEnvironment
import org.n1.av2.backend.model.Syntax
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.terminal.hacking.CommandHackService
import org.springframework.stereotype.Service

// FIXME: when the class is finished
@Suppress("UNUSED_PARAMETER")

@Service
class HackTerminalService(
        val stompService: StompService,
        val currentUserService: CurrentUserService,
        val socialTerminalService: SocialTerminalService,
        val commandHackService: CommandHackService,
        val environment: MyEnvironment) {

    fun processCommand(runId: String, command: String) {
        val tokens = command.split(" ")
        when (tokens[0]) {
            "help" -> processHelp()
            "mv" -> stompService.terminalReceive("[warn]Not implemented. Yet...")
            "hack" -> commandHackService.process(runId, tokens)
            "dc" -> stompService.terminalReceive("[warn]Not implemented. Yet...")
            "servererror" -> error("gah")
            "/share" -> socialTerminalService.processShare(runId, tokens)

            else -> stompService.terminalReceive("Unknown command, try [u]help[/].")
        }
    }

    private fun processHelp() {
        stompService.terminalReceive(
                "Command options:",
                " [u]mv [ok]<network id>[/]     -- for example: [u]mv[ok] 00",
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


    fun sendSyntaxHighlighting() {
        val map = HashMap<String, Syntax>()

        map["help"] = Syntax("u", "error s")
        map["mv"] = Syntax("u", "ok", "error s")
        map["view"] = Syntax("u", "error s")
        map["dc"] = Syntax("u", "error s")
        map["hack"] = Syntax("u", "primary", "error s")
        map["/share"] = Syntax("u warn", "info", "error s")

        stompService.toUser(ReduxActions.SERVER_TERMINAL_SYNTAX_HIGHLIGHTING, map)
    }


}