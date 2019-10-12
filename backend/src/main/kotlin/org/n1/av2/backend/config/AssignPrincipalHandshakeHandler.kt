package org.n1.av2.backend.config

import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.StompConnectionEventService
import org.springframework.http.server.ServerHttpRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

/**
 * determineUser is called when the Websocket connection is made.
 * It adds the unique client ID to the connection in order to check that every user has only one connection.
 */
@Component
class AssignPrincipalHandshakeHandler(
        private val stompConnectionEventService: StompConnectionEventService
) : DefaultHandshakeHandler() {

    override fun determineUser(request: ServerHttpRequest, wsHandler: WebSocketHandler?,
                               attributes: Map<String, Any>?): Principal {

        val principal = SecurityContextHolder.getContext().authentication as? UserPrincipal ?: throw RuntimeException("Login please")

        val validConnection = stompConnectionEventService.connect(principal)
        if (!validConnection) {
            principal.invalidate()
        }

        return principal
    }

}