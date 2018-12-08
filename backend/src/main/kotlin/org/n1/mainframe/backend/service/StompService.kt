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

    fun toSite(siteId: String, actionType: String, data: Any? = null) {
        simulateNonLocalhost()
        val event = ReduxEvent(actionType, data)
        stompTemplate.convertAndSend("/topic/site/${siteId}", event)
    }

    fun notyToUser(principal: Principal, message: NotyMessage) {
        simulateNonLocalhost()
        stompTemplate.convertAndSendToUser(principal.name, "/reply", message)
    }

//    fun errorToUser(text: String) {
//        simulateNonLocalhost()
//        val user = webSocketUserService.getCurrentUser()
//        stompTemplate.convertAndSendToUser(user, "/error", text)
//    }
}