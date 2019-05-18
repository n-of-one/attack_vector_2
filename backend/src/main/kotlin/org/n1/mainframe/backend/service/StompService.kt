package org.n1.mainframe.backend.service

import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.ReduxEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service

@Service
class StompService(
        val stompTemplate: SimpMessageSendingOperations,
        val principalService: PrincipalService) {

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

    fun toScan(scanId: String, actionType: ReduxActions, data: Any? = null) {
        simulateNonLocalhost()
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSend("/topic/scan/${scanId}", event)
    }

    fun toUser(actionType: ReduxActions, data: Any) {
        simulateNonLocalhost()
        val event = ReduxEvent(actionType, data)
        val userId = principalService.get().userId
        stompTemplate.convertAndSendToUser(userId, "/reply", event)
    }

    fun toUser(message: NotyMessage) {
        simulateNonLocalhost()
        val userId = principalService.get().userId
        stompTemplate.convertAndSendToUser(userId, "/noty", message)
    }

    data class TerminalReceive(val terminalId: String, val lines: Array<out String>)
    fun terminalReceive(vararg lines: String) {
        toUser(ReduxActions.SERVER_TERMINAL_RECEIVE, TerminalReceive("main", lines))
    }

    fun terminalReceiveForId(terminalId: String, vararg lines: String) {
        toUser(ReduxActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(terminalId, lines))
    }

}