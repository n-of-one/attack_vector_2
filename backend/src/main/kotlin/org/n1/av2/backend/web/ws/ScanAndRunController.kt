package org.n1.av2.backend.web.ws

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ReduxEvent
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.scan.ScanInfoService
import org.n1.av2.backend.util.toServerFatalReduxEvent
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller

@Controller
class ScanAndRunController(
    private val taskRunner: TaskRunner,
    private val scanInfoService: ScanInfoService,
    private val runService: RunService,
    private val hackerStateEntityService: HackerStateEntityService,
) {

    private val logger = mu.KotlinLogging.logger {}


    @MessageMapping("/scan/scansOfPlayer")
    fun scansOfPlayer(userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { scanInfoService.sendScanInfosOfPlayer() }
    }

    @MessageMapping("/scan/scanForName")
    fun scanForName(siteName: String, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { runService.startNewRun(siteName) }
    }

    @MessageMapping("/scan/deleteScan")
    fun deleteScan(runId: String, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { scanInfoService.deleteScan(runId) }
    }

    @MessageMapping("/scan/enterScan")
    fun enterScan(siteId: String, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { runService.enterRun(siteId) }
    }

    @MessageMapping("/run/leaveRun")
    fun leaveScan(runId: String, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) {
            val state = hackerStateEntityService.retrieveForCurrentUser()
            runService.leaveRun(state) }
    }

//     --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: Exception): ReduxEvent {
        logger.error(exception.message, exception)
        return toServerFatalReduxEvent(exception)
    }
}