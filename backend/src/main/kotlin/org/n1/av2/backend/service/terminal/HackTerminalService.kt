package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.config.MyEnvironment
import org.n1.av2.backend.model.Syntax
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.terminal.hacking.CommandHackService
import org.n1.av2.backend.service.terminal.hacking.CommandMoveService
import org.n1.av2.backend.service.terminal.hacking.CommandViewService
import org.springframework.stereotype.Service

@Service
class HackTerminalService(
        val stompService: StompService,
        val currentUserService: CurrentUserService,
        val socialTerminalService: SocialTerminalService,
        val commandHackService: CommandHackService,
        val commandMoveService: CommandMoveService,
        val commandViewService: CommandViewService,
        val environment: MyEnvironment) {

    fun processCommand(runId: String, command: String) {
        val tokens = command.split(" ")
        val commandAction = tokens[0].toLowerCase()
        when (commandAction) {
            "help" -> processHelp()
            "move" -> commandMoveService.process(runId, tokens)
            "hack" -> commandHackService.process(runId, tokens)
            "view" -> commandViewService.process(runId)
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
        map["move"] = Syntax("u", "ok", "error s")
        map["view"] = Syntax("u", "error s")
        map["hack"] = Syntax("u", "primary", "error s")
        map["dc"] = Syntax("u", "error s")
        map["/share"] = Syntax("u warn", "info", "error s")

        sendSyntaxHighlighting(map, stompService)
    }


}