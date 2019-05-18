package org.n1.mainframe.backend.service.scan

import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class ScanTerminalService(val scanningService: ScanningService,
                          val stompService: StompService) {

    fun processCommand(scanId: String, command: String) {
        val tokens = command.split(" ")
        processCommand(scanId, tokens)
    }

    fun processCommand(scanId: String, tokens: List<String>) {
        if (tokens[0] == "help") {
            stompService.terminalReceive(
                    "Command options:",
                    " autoscan",
                    " scan",
                    " scan [ok]<network id>[/]   -- for example: scan 00",
                    " dc")
            return
        }
        if (tokens[0] == "dc") {
            data class Navigate(val target: String)
            stompService.toUser(ReduxActions.SERVER_NAVIGATE_PAGE, Navigate("HACKER_HOME"))
            return
        }
        if (tokens[0] == "autoscan") {
            scanningService.launchProbe(scanId, true)
            return
        }
        if (tokens[0] == "scan") {
            processCommandScan(scanId, tokens)
            return
        }
        stompService.terminalReceive("Unknown command, try [i]help[/].")
    }

    fun processCommandScan(scanId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            scanningService.launchProbe(scanId, false)
            return
        }
        if (tokens.size == 2) {
            val networkId = tokens[1]
            scanningService.launchProbeAtNode(scanId, networkId)
            return
        }
        stompService.terminalReceive("scan takes 0 or 1 arguments.")
    }

}