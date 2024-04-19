package org.n1.av2.backend.web.ws

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ReduxEvent
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.site.RunLinkService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.util.toServerFatalReduxEvent
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller

@Controller
class RunController(
    private val userTaskRunner: UserTaskRunner,
    private val runLinkService: RunLinkService,
    private val runService: RunService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val siteService: SiteService,
    private val stompService: StompService

    ) {

    private val logger = mu.KotlinLogging.logger {}


    @MessageMapping("/run/sendRunInfosToUser")
    fun scansOfPlayer(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { runLinkService.sendRunInfosToUser() }
    }

    @MessageMapping("/run/newRun")
    fun scanForName(siteName: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { runService.startNewRun(siteName) }
    }

    @MessageMapping("/scan/deleteScan")
    fun deleteScan(runId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { runLinkService.deleteRunLink(runId) }
    }


    @MessageMapping("/run/prepareToEnterRun")
    fun prepareToEnterRun(runId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { runService.prepareToEnterRun(runId) }
    }

    @MessageMapping("/run/enterRun")
    fun enterRun(runId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { runService.enterRun(userPrincipal.userId, runId, userPrincipal.connectionId) }
    }


    @MessageMapping("/run/leaveSite")
    fun leaveScan(runId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            val state = hackerStateEntityService.retrieveForCurrentUser()
            runService.leaveSite(state, true) }
    }

    @MessageMapping("/run/getTimers")
    fun getTimers(runId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            runService.getTimers(runId, userPrincipal.userId) }
    }


//     --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: Exception): ReduxEvent {
        logger.error(exception.message, exception)
        return toServerFatalReduxEvent(exception)
    }
}