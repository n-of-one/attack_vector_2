package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.service.scan.ScanTerminalService
import org.springframework.stereotype.Service

@Service
class TerminalService(val scanTerminalService: ScanTerminalService) {

    fun processCommand(runId: String, command: String) {
        scanTerminalService.processCommand(runId, command)
    }
}