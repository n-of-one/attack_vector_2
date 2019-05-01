package org.n1.mainframe.backend.service

import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.ReduxEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class StompService(
        val stompTemplate: SimpMessageSendingOperations) {

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

    fun toUser(principal: Principal, actionType: ReduxActions, data: Any) {
        simulateNonLocalhost()
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSendToUser(principal.name, "/reply", event)
    }

    fun toUser(principal: Principal, message: NotyMessage) {
        simulateNonLocalhost()
        val event = ReduxEvent(ReduxActions.SERVER_NOTIFICATION, message)
        stompTemplate.convertAndSendToUser(principal.name, "/reply", event)
    }

    fun errorToUser(principal: Principal, message: NotyMessage) {
        simulateNonLocalhost()
        stompTemplate.convertAndSendToUser(principal.name, "/error", message)
    }

    fun terminalReceive(principal: Principal, line: String) {
        toUser(principal, ReduxActions.SERVER_TERMINAL_RECEIVE, line)
    }

}