package org.n1.mainframe.backend.service.scan

import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class ScanTerminalService(val scanningService: ScanningService,
                          val stompService: StompService) {

    fun processCommand(scanId: String, command: String, principal: Principal) {
        val tokens = command.split(" ")
        processCommand(scanId, tokens, principal)
    }

    fun processCommand(scanId: String, tokens: List<String>, principal: Principal) {
        if (tokens[0] == "help") {
            stompService.terminalReceive(principal,
                    "Command options:",
                    " autoscan",
                    " scan",
                    " scan [ok]<network id>[/]   -- for example: scan 00",
                    " dc")
            return
        }
        if (tokens[0] == "dc") {
            data class Navigate(val target: String)
            stompService.toUser(principal, ReduxActions.SERVER_NAVIGATE_PAGE, Navigate("HACKER_HOME"))
            return
        }
        if (tokens[0] == "autoscan") {
            scanningService.launchProbe(scanId, true, principal)
            return
        }
        if (tokens[0] == "scan") {
            processCommandScan(scanId, tokens, principal)
            scanningService.launchProbe(scanId, false, principal)
            return
        }
        stompService.terminalReceive(principal, "Unknown command, try [i]help[/].")
    }

    fun processCommandScan(scanId: String, tokens: List<String>, principal: Principal) {
        if (tokens.size == 1) {
            scanningService.launchProbe(scanId, false, principal)
        }
        if (tokens.size == 2) {
            val networkId = tokens[1]
            scanningService.launchProbeAtNode(scanId, networkId, principal)
            return
        }
        stompService.terminalReceive(principal, "scan takes 0 or 1 arguments.")
    }

}