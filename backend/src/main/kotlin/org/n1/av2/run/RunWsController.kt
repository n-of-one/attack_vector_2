package org.n1.av2.run

import org.n1.av2.frontend.model.ReduxEvent
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.util.toServerFatalReduxEvent
import org.n1.av2.run.runlink.RunLinkService
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller

@Controller
class RunWsController(
    private val userTaskRunner: UserTaskRunner,
    private val runLinkService: RunLinkService,
    private val runService: RunService,
    private val hackerStateEntityService: HackerStateEntityService,

    ) {

    private val logger = mu.KotlinLogging.logger {}


    @MessageMapping("/run/sendRunInfosToUser")
    fun scansOfPlayer(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { runLinkService.sendRunInfosToUser() }
    }

    @MessageMapping("/run/newRun")
    fun scanForName(siteName: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { runService.startNewRunAndReply(siteName) }
    }

    @MessageMapping("/run/deleteRunLink")
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
