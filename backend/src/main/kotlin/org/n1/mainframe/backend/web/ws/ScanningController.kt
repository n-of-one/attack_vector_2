package org.n1.mainframe.backend.web.ws

import mu.KLogging
import org.n1.mainframe.backend.engine.SerializingExecutor
import org.n1.mainframe.backend.model.scan.NodeScanType
import org.n1.mainframe.backend.model.ui.ReduxEvent
import org.n1.mainframe.backend.service.scan.ScanTerminalService
import org.n1.mainframe.backend.service.scan.ScanningService
import org.n1.mainframe.backend.util.toServerFatalReduxEvent
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ScanningController(
        val executor: SerializingExecutor,
        val scanningService: ScanningService,
        val scanTerminalService: ScanTerminalService
) {

    companion object: KLogging()


    @MessageMapping("/scan/scansOfPlayer")
    fun scansOfPlayer(principal: Principal) {
        executor.run(principal) { scanningService.scansOfPlayer() }
    }

    @MessageMapping("/scan/enterScan")
    fun siteFull(siteId: String, principal: Principal) {
        executor.run(principal) { scanningService.enterScan(siteId) }
    }

    data class TerminalCommand(val scanId: String, val command: String)
    @MessageMapping("/scan/terminal")
    fun terminal(terminalCommand: TerminalCommand, principal: Principal) {
        executor.run(principal) { scanTerminalService.processCommand(terminalCommand.scanId, terminalCommand.command) }
    }

    @MessageMapping("/scan/autoScan")
    fun autoScan(scanId: String, principal: Principal) {
        executor.run(principal) { scanningService.autoScan(scanId) }
    }


    data class ProbeScanActionInput(val scanId: String, val nodeId: String, val action: NodeScanType)
    @MessageMapping("/scan/probeArrive")
    fun probeArrive(input: ProbeScanActionInput, principal: Principal) {
        executor.run(principal) { scanningService.probeArrive(input.scanId, input.nodeId, input.action) }
    }
//     --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: Exception): ReduxEvent {
        logger.error(exception.message, exception)
        return toServerFatalReduxEvent(exception)
    }
}