package org.n1.av2.backend.service

import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.ReduxEvent
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.terminal.TERMINAL_MAIN
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class StompService(
        val stompTemplate: SimpMessageSendingOperations,
        val currentUserService: CurrentUserService) {

    private val logger = mu.KotlinLogging.logger {}

    @Value("\${ENVIRONMENT ?: default}")
    lateinit var environment: String

    val simulateNonLocalhost = {
        if (environment.startsWith("dev")) {
            Thread.sleep(70)
        }
    }

    fun toSite(siteId: String, actionType: ServerActions, data: Any? = null) {
        simulateNonLocalhost()
        logger.debug("-> ${siteId} ${actionType}")
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSend("/topic/site/${siteId}", event)
    }

    fun toRun(runId: String, actionType: ServerActions, data: Any? = null) {
        simulateNonLocalhost()
        logger.debug("-> ${runId} ${actionType}")
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSend("/topic/run/${runId}", event)
    }

    fun toIce(iceId: String, actionType: ServerActions, data: Any? = null) {
        simulateNonLocalhost()
        logger.debug("-> ${iceId} ${actionType}")
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSend("/topic/ice/${iceId}", event)
    }

    fun reply(actionType: ServerActions, data: Any) {
        simulateNonLocalhost()
        val name = SecurityContextHolder.getContext().authentication.name
        logger.debug("-> ${name} ${actionType}")
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSendToUser(name, "/reply", event)
    }

    fun replyMessage(message: NotyMessage) {
        reply(ServerActions.SERVER_NOTIFICATION, message)
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

    fun toUser(userId: String, actionType: ServerActions, data: Any) {
        simulateNonLocalhost()
        logger.debug("-> ${userId} ${actionType}")
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSend("/topic/user/${userId}", event)
    }

}