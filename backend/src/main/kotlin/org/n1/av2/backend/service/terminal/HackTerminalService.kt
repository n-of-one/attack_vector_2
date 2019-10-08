package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.config.MyEnvironment
import org.n1.av2.backend.model.Syntax
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.HackerPositionService
import org.n1.av2.backend.service.terminal.hacking.CommandHackService
import org.n1.av2.backend.service.terminal.hacking.CommandMoveService
import org.n1.av2.backend.service.terminal.hacking.CommandViewService
import org.springframework.stereotype.Service

@Service
class HackTerminalService(
        private val stompService: StompService,
        private val currentUserService: CurrentUserService,
        private val socialTerminalService: SocialTerminalService,
        private val commandHackService: CommandHackService,
        private val commandMoveService: CommandMoveService,
        private val commandViewService: CommandViewService,
        private val hackerPositionService: HackerPositionService,
        private val environment: MyEnvironment) {

    fun processCommand(runId: String, command: String) {
        val tokens = command.trim().split(" ")
        val commandAction = tokens[0].toLowerCase()


        when (commandAction) {
            "help" -> processHelp()
            "dc" -> stompService.terminalReceive("[warn]Not implemented. Yet...")
            "servererror" -> error("gah")
            "/share" -> socialTerminalService.processShare(runId, tokens)
            else -> processPrivilegedCommand(runId, tokens, commandAction)
        }
    }

    private fun processPrivilegedCommand(runId: String, tokens: List<String>, commandAction: String) {
        val position = hackerPositionService.retrieveForCurrentUser()
        if (position.locked) return reportLocked()
        if (position.inTransit) return reportInTransit()

        when (commandAction) {
            "move" -> commandMoveService.processCommand(runId, tokens, position)
            "hack" -> commandHackService.process(runId, tokens, position)
            "view" -> commandViewService.process(runId, position)
            "/share" -> socialTerminalService.processShare(runId, tokens)

            else -> stompService.terminalReceive("Unknown command, try [u]help[/].")
        }
    }

    private fun processHelp() {
        stompService.terminalReceive(
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
        stompService.terminalReceive("[error]critical[/] OS refuses operation with error message [error]unauthorized[/].")
    }

    private fun reportInTransit() {
        stompService.terminalReceive("[error]busy[/] current move not finished.")
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