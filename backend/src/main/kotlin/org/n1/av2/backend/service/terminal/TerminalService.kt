package org.n1.av2.backend.service.terminal

import mu.KLogging
import org.n1.av2.backend.model.db.run.HackerSpecificActivity.*
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.HackerStateService
import org.springframework.stereotype.Service

@Service
class TerminalService(
        private val hackerStateService: HackerStateService,
        private val scanTerminalService: ScanTerminalService,
        private val hackTerminalService: HackTerminalService,
        private val stompService: StompService) {

    private companion object : KLogging()

    fun processCommand(runId: String, command: String) {
        if (command.trim().isBlank()) {
            return
        }
        val type = hackerStateService.retrieveForCurrentUser().specificActivity
        when (type) {
            SCANNING -> scanTerminalService.processCommand(runId, command)
            AT_NODE -> hackTerminalService.processCommand(runId, command)
            MOVING,
            STARTING -> reportInTransit()

            else -> {
                logger.error("Received terminal command for user that is doing: ${type}")
            }
        }
    }

    private fun reportInTransit() {
        stompService.terminalReceiveCurrentUser("[error]busy[/] current move not finished.")
    }


}