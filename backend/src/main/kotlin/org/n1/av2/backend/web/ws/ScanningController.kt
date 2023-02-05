package org.n1.av2.backend.web.ws

import mu.KLogging
import org.n1.av2.backend.engine.TaskRunner
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
    val taskRunner: TaskRunner,
    val scanningService: ScanningService
) {

    companion object: KLogging()


    @MessageMapping("/scan/scansOfPlayer")
    fun scansOfPlayer(principal: Principal) {
        taskRunner.runTask(principal) { scanningService.sendScanInfosOfPlayer() }
    }

    @MessageMapping("/scan/scanForName")
    fun scanForName(siteName: String, principal: Principal) {
        taskRunner.runTask(principal) { scanningService.scanSiteForName(siteName) }
    }

    @MessageMapping("/scan/deleteScan")
    fun deleteScan(runId: String, principal: Principal) {
        taskRunner.runTask(principal) { scanningService.deleteScan(runId) }
    }

    @MessageMapping("/scan/enterScan")
    fun enterScan(siteId: String, principal: Principal) {
        taskRunner.runTask(principal) { scanningService.enterScan(siteId) }
    }

    @MessageMapping("/scan/leaveScan")
    fun leaveScan(runId: String, principal: Principal) {
        taskRunner.runTask(principal) { scanningService.leaveRun(runId) }
    }

    data class ProbeScanActionInput(val runId: String, val nodeId: String, val action: NodeScanType, val autoScan: Boolean)
    @MessageMapping("/scan/probeArrive")
    fun probeArrive(input: ProbeScanActionInput, principal: Principal) {
        taskRunner.runTask(principal) { scanningService.probeArrive(input.runId, input.nodeId, input.action, input.autoScan) }
    }
//     --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: Exception): ReduxEvent {
        logger.error(exception.message, exception)
        return toServerFatalReduxEvent(exception)
    }
}