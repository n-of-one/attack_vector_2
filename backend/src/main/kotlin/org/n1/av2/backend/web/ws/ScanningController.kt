package org.n1.av2.backend.web.ws

import mu.KLogging
import org.n1.av2.backend.engine.SerializingExecutor
import org.n1.av2.backend.model.ui.NodeScanType
import org.n1.av2.backend.model.ui.ReduxEvent
import org.n1.av2.backend.service.scan.ScanningService
import org.n1.av2.backend.util.toServerFatalReduxEvent
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

    companion object: KLogging()


    @MessageMapping("/scan/scansOfPlayer")
    fun scansOfPlayer(principal: Principal) {
        executor.run(principal) { scanningService.sendScanInfosOfPlayer() }
    }

    @MessageMapping("/scan/scanForName")
    fun scanForName(siteName: String, principal: Principal) {
        executor.run(principal) { scanningService.scanSiteForName(siteName) }
    }

    @MessageMapping("/scan/deleteScan")
    fun deleteScan(runId: String, principal: Principal) {
        executor.run(principal) { scanningService.deleteScan(runId) }
    }

    @MessageMapping("/scan/enterScan")
    fun enterScan(siteId: String, principal: Principal) {
        executor.run(principal) { scanningService.enterScan(siteId) }
    }

    @MessageMapping("/scan/leaveScan")
    fun leaveScan(runId: String, principal: Principal) {
        executor.run(principal) { scanningService.leaveRun(runId) }
    }

    data class AutoScanActionInput(val runId: String)
    @MessageMapping("/scan/autoScan")
    fun autoScan(action: AutoScanActionInput, principal: Principal) {
        executor.run(principal) { scanningService.autoScan(action.runId) }
    }

    data class ProbeScanActionInput(val runId: String, val nodeId: String, val action: NodeScanType)
    @MessageMapping("/scan/probeArrive")
    fun probeArrive(input: ProbeScanActionInput, principal: Principal) {
        executor.run(principal) { scanningService.probeArrive(input.runId, input.nodeId, input.action) }
    }
//     --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: Exception): ReduxEvent {
        logger.error(exception.message, exception)
        return toServerFatalReduxEvent(exception)
    }
}