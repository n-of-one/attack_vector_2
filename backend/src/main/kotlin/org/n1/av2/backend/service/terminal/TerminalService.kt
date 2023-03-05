package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.run.RunActivity.AT_NODE
import org.n1.av2.backend.entity.run.RunActivity.SCANNING
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

const val TERMINAL_MAIN = "main"
const val TERMINAL_CHAT = "chat"


@Service
class TerminalService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val scanTerminalService: ScanTerminalService,
    private val hackTerminalService: HackTerminalService,
    private val stompService: StompService) {

    private val logger = mu.KotlinLogging.logger {}

    fun processCommand(runId: String, command: String) {
        if (command.trim().isBlank()) {
            return
        }
        val type = hackerStateEntityService.retrieveForCurrentUser().runActivity
        when (type) {
            SCANNING -> scanTerminalService.processCommand(runId, command)
            AT_NODE -> hackTerminalService.processCommand(runId, command)

            else -> {
                logger.error("Received terminal command for user that is doing: ${type}")
            }
        }
    }


}