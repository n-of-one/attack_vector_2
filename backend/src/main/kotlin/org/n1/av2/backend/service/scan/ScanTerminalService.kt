package org.n1.av2.backend.service.scan

import org.n1.av2.backend.config.MyEnvironment
import org.n1.av2.backend.service.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.user.UserService
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class ScanTerminalService(val scanningService: ScanningService,
                          val stompService: StompService,
                          val userService: UserService,
                          val environment: MyEnvironment) {

    fun processCommand(runId: String, command: String) {
        val tokens = command.split(" ")
        when (tokens[0]) {
            "help" -> processHelp()
            "dc" -> processDisconnect()
            "autoscan" -> processAutoScan(runId)
            "scan" -> processScan(runId, tokens)
            "/share" -> processShare(runId, tokens)
            "servererror" -> error("gah")
            "quickscan" -> processQuickscan(runId)
            else -> stompService.terminalReceive("Unknown command, try [u]help[/].")
        }
    }

    private fun processAutoScan(runId: String) {
        stompService.terminalReceive("Autoscan started. [i]Click on nodes for information retreived by scan.[/]")
        scanningService.launchProbe(runId, true)
    }

    private fun processDisconnect() {
        stompService.toUser(ReduxActions.SERVER_USER_DC, "-")
    }

    private fun processHelp() {
        stompService.terminalReceive(
                "Command options:",
                " [u]autoscan",
                " [u]attack",
                " [u]scan [ok]<network id>[/]   -- for example: [u]scan [ok]00",
                " [u]dc",
                " [u]/share [info]<user name>")
        if (environment.dev) {
            stompService.terminalReceive(
                    "",
                    "[i]Available only during development and testing:[/]",
                    " [u]quickscan"
            )
        }
    }

    fun processScan(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            stompService.terminalReceive("Missing [ok]<network id>[/], for example: [u]scan[/] [ok]00[/] . Or did you mean [u]autoscan[/]?")
            return
        }
        val networkId = tokens[1]
        scanningService.launchProbeAtNode(runId, networkId)
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
        scanningService.shareScan(runId, user)
    }

    fun processQuickscan(runId: String) {
        scanningService.quickScan(runId)
    }

}