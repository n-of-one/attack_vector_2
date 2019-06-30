package org.n1.av2.backend.service.terminal

import mu.KLogging
import org.n1.av2.backend.model.hacker.HackerActivityType
import org.n1.av2.backend.service.user.HackerActivityService
import org.springframework.stereotype.Service

@Service
class TerminalService(
        val userActivityService: HackerActivityService,
        val scanTerminalService: ScanTerminalService,
        val hackTerminalService: HackTerminalService
) {

    companion object: KLogging()

    fun processCommand(runId: String, command: String) {
        if (command.trim().isBlank()) {
            return
        }
        val type = userActivityService.currentActivity()
        when (type) {
            HackerActivityType.SCANNING -> scanTerminalService.processCommand(runId, command)
            HackerActivityType.HACKING -> hackTerminalService.processCommand(runId, command)
            else -> {
                logger.error("Received terminal command for user that is doing: ${type}")
            }
        }
    }
}