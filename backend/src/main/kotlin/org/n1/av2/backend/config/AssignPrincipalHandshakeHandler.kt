package org.n1.av2.backend.config

import org.n1.av2.backend.model.iam.UserPrincipal
import org.springframework.http.server.ServerHttpRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

/**
 * determineUser is called when the Websocket connection is made.
 * It adds the unique client ID to the connection in order to check that every user has only one connection.
 */
class AssignPrincipalHandshakeHandler : DefaultHandshakeHandler() {

    override fun determineUser(request: ServerHttpRequest, wsHandler: WebSocketHandler?,
                               attributes: Map<String, Any>?): Principal {

        return SecurityContextHolder.getContext().authentication as? UserPrincipal ?: throw RuntimeException("Login please")
    }

}