package org.n1.av2.backend.service

import org.n1.av2.backend.model.db.user.User
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.ReduxEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service

@Service
class StompService(
        val stompTemplate: SimpMessageSendingOperations,
        val currentUserService: CurrentUserService) {

    @Value("\${ENVIRONMENT ?: default}")
    lateinit var environment: String

    val simulateNonLocalhost = {
        if (environment.startsWith("dev")) {
            Thread.sleep(70)
        }
    }

    fun toSite(siteId: String, actionType: ReduxActions, data: Any? = null) {
        simulateNonLocalhost()
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSend("/topic/site/${siteId}", event)
    }

    fun toRun(runId: String, actionType: ReduxActions, data: Any? = null) {
        simulateNonLocalhost()
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSend("/topic/run/${runId}", event)
    }

    fun toUser(actionType: ReduxActions, data: Any) {
        val userId = currentUserService.userId
        toUser(userId, actionType, data)
    }

    fun toUser(userId: String, actionType: ReduxActions, data: Any) {
        simulateNonLocalhost()
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSendToUser(userId, "/reply", event)
    }

    fun toUser(message: NotyMessage) {
        val userId = currentUserService.userId
        toUser(userId, message)
    }

    fun toUser(userId: String, message: NotyMessage) {
        toUser(userId, ReduxActions.SERVER_NOTIFICATION, message)
    }

    data class TerminalReceive(val terminalId: String, val lines: Array<out String>)
    fun terminalReceive(vararg lines: String) {
        toUser(ReduxActions.SERVER_TERMINAL_RECEIVE, TerminalReceive("main", lines))
    }

    fun terminalReceiveForId(terminalId: String, vararg lines: String) {
        toUser(ReduxActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(terminalId, lines))
    }
    fun terminalReceiveForId(user: User, terminalId: String, vararg lines: String) {
        toUser(user.id, ReduxActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(terminalId, lines))
    }

}