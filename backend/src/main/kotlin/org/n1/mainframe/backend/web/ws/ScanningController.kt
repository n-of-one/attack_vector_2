package org.n1.mainframe.backend.web.ws

import org.n1.mainframe.backend.engine.SerializingExecutor
import org.n1.mainframe.backend.model.scan.NodeScanType
import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.ValidationException
import org.n1.mainframe.backend.service.scan.ScanningService
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ScanningController(
        val executor: SerializingExecutor,
        val scanningService: ScanningService
) {

    @MessageMapping("/scan/sendScan")
    fun siteFull(siteId: String, principal: Principal) {
        executor.run(principal) { scanningService.sendScanToUser(siteId, principal) }
    }

    data class TerminalCommand(val scanId: String, val command: String)
    @MessageMapping("/scan/terminal")
    fun terminal(terminalCommand: TerminalCommand, principal: Principal) {
        executor.run(principal) { scanningService.processCommand(terminalCommand.scanId, terminalCommand.command, principal) }
    }

    @MessageMapping("/scan/autoScan")
    fun autoScan(scanId: String, principal: Principal) {
        executor.run(principal) { scanningService.autoScan(scanId, principal) }
    }


    data class ProbeScanActionInput(val scanId: String, val nodeId: String, val action: NodeScanType)
    @MessageMapping("/scan/probeArrive")
    fun probeArrive(input: ProbeScanActionInput, principal: Principal) {
        executor.run(principal) { scanningService.probeArrive(input.scanId, input.nodeId, input.action, principal) }
    }
    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    @MessageExceptionHandler
    @SendToUser("/error")
    fun handleException(exception: Exception): NotyMessage {
        if (exception is ValidationException) {
            return exception.getNoty()
        }
        EditorController.logger.error(exception.message, exception)
        return NotyMessage("fatal", "Server error", exception.message ?: "")
    }
}