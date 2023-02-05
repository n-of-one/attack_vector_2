package org.n1.av2.backend.service

import mu.KLogging
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.model.ui.ReduxEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service

@Service
class StompService(
        val stompTemplate: SimpMessageSendingOperations,
        val currentUserService: CurrentUserService) {

    companion object : KLogging()

    @Value("\${ENVIRONMENT ?: default}")
    lateinit var environment: String

    val simulateNonLocalhost = {
        if (environment.startsWith("dev")) {
            Thread.sleep(70)
        }
    }

    fun toSite(siteId: String, actionType: ReduxActions, data: Any? = null) {
        simulateNonLocalhost()
        logger.debug("-> ${siteId} ${actionType}")
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSend("/topic/site/${siteId}", event)
    }

    fun toRun(runId: String, actionType: ReduxActions, data: Any? = null) {
        simulateNonLocalhost()
        logger.debug("-> ${runId} ${actionType}")
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSend("/topic/run/${runId}", event)
    }

    fun toUser(userId: String, actionType: ReduxActions, data: Any) {
        simulateNonLocalhost()
        logger.debug("-> ${userId} ${actionType}")
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSendToUser(userId, "/reply", event)
    }

    fun toUser(actionType: ReduxActions, data: Any) {
        val userId = currentUserService.userId
        toUser(userId, actionType, data)
    }

    fun toUser(message: NotyMessage) {
        val userId = currentUserService.userId
        toUser(userId, message)
    }

    fun toUser(userId: String, message: NotyMessage) {
        toUser(userId, ReduxActions.SERVER_NOTIFICATION, message)
    }

    class TerminalReceive(val terminalId: String, val lines: Array<out String>, val locked : Boolean? = null)

    fun terminalReceiveAndLockedCurrentUser(locked: Boolean, vararg lines: String) {
        toUser(ReduxActions.SERVER_TERMINAL_RECEIVE, TerminalReceive("main", lines, locked))
    }

    fun terminalReceiveCurrentUser(vararg lines: String) {
        toUser(ReduxActions.SERVER_TERMINAL_RECEIVE, TerminalReceive("main", lines))
    }

    fun terminalLockCurrentUser(lock: Boolean) {
        toUser(ReduxActions.SERVER_TERMINAL_RECEIVE, TerminalReceive("main", emptyArray(), lock))
    }


    class TerminalReceiveLinesAsCollection(val terminalId: String, val lines: Collection<String>)
    fun terminalReceiveCurrentUser(lines: Collection<String>) {
        toUser(ReduxActions.SERVER_TERMINAL_RECEIVE, TerminalReceiveLinesAsCollection("main", lines))
    }

    fun terminalReceiveForUser(userId: String, vararg lines: String) {
        toUser(userId, ReduxActions.SERVER_TERMINAL_RECEIVE, TerminalReceive("main", lines))
    }

    fun terminalReceiveForUserForTerminal(userId: String, terminalId: String, vararg lines: String) {
        toUser(userId, ReduxActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(terminalId, lines))
    }

}