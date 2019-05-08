package org.n1.mainframe.backend.config

import org.n1.mainframe.backend.model.iam.UserAuthentication
import org.springframework.http.server.ServerHttpRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

class AssignPrincipalHandshakeHandler : DefaultHandshakeHandler() {

    override fun determineUser(request: ServerHttpRequest, wsHandler: WebSocketHandler?,
                               attributes: Map<String, Any>?): Principal {

        val authentication = SecurityContextHolder.getContext().authentication as? UserAuthentication ?: throw RuntimeException("Login please")
        authentication.generateClientId()
        return authentication
    }

}