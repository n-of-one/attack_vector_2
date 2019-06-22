package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.config.MyEnvironment
import org.n1.av2.backend.model.Syntax
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.scan.ScanningService
import org.n1.av2.backend.service.user.UserService
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class HackTerminalService(
        val scanningService: ScanningService,
        val stompService: StompService,
        val currentUserService: CurrentUserService,
        val userService: UserService,
        val environment: MyEnvironment) {

    fun processCommand(runId: String, command: String) {
        val tokens = command.split(" ")
        when (tokens[0]) {
            "help" -> processHelp()
            "mv" -> processMove(runId, tokens)
            "hack" -> processHack(runId, tokens)
            "dc" -> processDisconnect()
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

    fun processShare(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            stompService.terminalReceive("Share this scan with who?  -- try [u warn]/share [info]<username>[/].")
            return
        }
        val userName = tokens.stream().skip(1).collect(Collectors.joining(" "))
        val user = userService.findByName(userName)
        if (user == null) {
            stompService.terminalReceive("user [info]${userName}[/] not found.")
            return
        }
        scanningService.shareScan(runId, user)    }

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