package org.n1.mainframe.backend.service.scan

import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class ScanTerminalService(val scanningService: ScanningService,
                          val stompService: StompService) {

    fun processCommand(scanId: String, command: String) {
        val tokens = command.split(" ")
        when (tokens[0]) {
            "help" -> processHelp()
            "dc" -> processDisconnect()
            "autoscan" -> processAutoScan(scanId)
            "scan" -> processCommandScan(scanId, tokens)
            else -> stompService.terminalReceive("Unknown command, try [i]help[/].")
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