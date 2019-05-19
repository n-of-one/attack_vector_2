package org.n1.mainframe.backend.service.scan

import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.n1.mainframe.backend.service.user.UserService
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class ScanTerminalService(val scanningService: ScanningService,
                          val stompService: StompService,
                          val userService: UserService) {

    fun processCommand(scanId: String, command: String) {
        val tokens = command.split(" ")
        when (tokens[0]) {
            "help" -> processHelp()
            "dc" -> processDisconnect()
            "autoscan" -> processAutoScan(scanId)
            "scan" -> processScan(scanId, tokens)
            "/share" -> processShare(scanId, tokens)
            "servererror" -> error("gah")
            else -> stompService.terminalReceive("Unknown command, try [u]help[/].")
        }
    }

    private fun processAutoScan(scanId: String) {
        scanningService.launchProbe(scanId, true)
    }

    private fun processDisconnect() {
        data class Navigate(val target: String)
        stompService.toUser(ReduxActions.SERVER_NAVIGATE_PAGE, Navigate("HACKER_HOME"))
    }

    private fun processHelp() {
        stompService.terminalReceive(
                "Command options:",
                " [u]autoscan",
                " [u]scan",
                " [u]scan [ok]<network id>[/]   -- for example: [u]scan [ok]00",
                " [u]dc")
    }

    fun processScan(scanId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            scanningService.launchProbe(scanId, false)
            return
        }
        val networkId = tokens[1]
        scanningService.launchProbeAtNode(scanId, networkId)
    }

    fun processShare(scanId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            stompService.terminalReceive("Share this scan with who?  -- try [u warn]/share [info]<username>[/].")
            return
        }
        val userName = tokens.stream().skip(1).collect( Collectors.joining(" ") )
        val user = userService.findByName(userName)
        if (user == null) {
            stompService.terminalReceive("user [info]${userName}[/] not found.")
            return
        }
        scanningService.shareScan(scanId, user)

    }

}