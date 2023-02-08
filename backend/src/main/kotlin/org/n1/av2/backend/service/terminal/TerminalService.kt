package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.model.db.run.RunActivity.AT_NODE
import org.n1.av2.backend.model.db.run.RunActivity.SCANNING
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.HackerStateService
import org.springframework.stereotype.Service

@Service
class TerminalService(
        private val hackerStateService: HackerStateService,
        private val scanTerminalService: ScanTerminalService,
        private val hackTerminalService: HackTerminalService,
        private val stompService: StompService) {

    private val logger = mu.KotlinLogging.logger {}

    fun processCommand(runId: String, command: String) {
        if (command.trim().isBlank()) {
            return
        }
        val type = hackerStateService.retrieveForCurrentUser().runActivity
        when (type) {
            SCANNING -> scanTerminalService.processCommand(runId, command)
            AT_NODE -> hackTerminalService.processCommand(runId, command)

            else -> {
                logger.error("Received terminal command for user that is doing: ${type}")
            }
        }
    }


}