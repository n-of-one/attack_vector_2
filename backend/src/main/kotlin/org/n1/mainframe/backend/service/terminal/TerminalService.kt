package org.n1.mainframe.backend.service.terminal

import org.n1.mainframe.backend.service.scan.ScanTerminalService
import org.springframework.stereotype.Service

@Service
class TerminalService(val scanTerminalService: ScanTerminalService) {

    fun processCommand(scanId: String, command: String) {
        scanTerminalService.processCommand(scanId, command)
    }
}