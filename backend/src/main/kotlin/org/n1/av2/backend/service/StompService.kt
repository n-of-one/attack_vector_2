package org.n1.av2.backend.service

import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ReduxEvent
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.terminal.TERMINAL_MAIN
import org.n1.av2.backend.service.user.CurrentUserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class StompService(
        val stompTemplate: SimpMessageSendingOperations,
        val currentUserService: CurrentUserService
) {

    private val logger = mu.KotlinLogging.logger {}

    @Value("\${ENVIRONMENT:default}")
    lateinit var environment: String

    @PostConstruct
    fun logEnvironment() {
        logger.info("ENVIRONMENT: ${environment}")
        if (environment.startsWith("dev")) {
            logger.info("Simulating non-localhost")
        }
    }

    val simulateNonLocalhost = {
        if (environment.startsWith("dev")) {
            Thread.sleep(70)
        }
    }

    private fun sendToDestination(path: String, actionType: ServerActions, data: Any? = null) {
        simulateNonLocalhost()
        logger.debug("-> ${path} ${actionType}")
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSend(path, event)
    }

    fun toSite(siteId: String, actionType: ServerActions, data: Any? = null) {
        sendToDestination("/topic/site/${siteId}", actionType, data)
    }

    fun toRun(runId: String, actionType: ServerActions, data: Any? = null) {
        sendToDestination("/topic/topic/${runId}", actionType, data)
    }

    fun toIce(iceId: String, actionType: ServerActions, data: Any? = null) {
        sendToDestination("/topic/ice/${iceId}", actionType, data)
    }

    fun toApp(appId: String, actionType: ServerActions, data: Any) {
        sendToDestination("/topic/app/${appId}", actionType, data)
    }

    fun toUser(userId: String, actionType: ServerActions, data: Any) {
        sendToDestination("/topic/user/${userId}", actionType, data)
    }

    fun reply(actionType: ServerActions, data: Any) {
        val name = SecurityContextHolder.getContext().authentication.name
        logger.debug("-> ${name} ${actionType}")

        simulateNonLocalhost()
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSend("/reply", event)
    }

    //**********************************************************************************************************************//

    fun replyMessage(message: NotyMessage) {
        reply(ServerActions.SERVER_NOTIFICATION, message)
    }

    fun replyNeutral(message: String, title: String = "") {
        reply(ServerActions.SERVER_NOTIFICATION, NotyMessage(NotyType.NEUTRAL, title, message))
    }

    fun replyError(message: String, title: String = "") {
        reply(ServerActions.SERVER_NOTIFICATION, NotyMessage(NotyType.ERROR, title, message))
    }

    class TerminalReceive(val terminalId: String, val lines: Array<out String>, val locked : Boolean? = null)

    fun replyTerminalReceiveAndLocked(locked: Boolean, vararg lines: String) {
        reply(ServerActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, lines, locked))
    }

    fun replyTerminalReceive(vararg lines: String) {
        reply(ServerActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, lines ))
    }

    fun replyTerminalSetLocked(lock: Boolean) {
        reply(ServerActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, emptyArray(), lock))
    }


}