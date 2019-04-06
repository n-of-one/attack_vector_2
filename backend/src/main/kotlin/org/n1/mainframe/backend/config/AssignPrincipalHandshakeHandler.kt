package org.n1.mainframe.backend.config

import org.n1.mainframe.backend.model.user.ClientPrincipal
import org.n1.mainframe.backend.model.user.UserAuthentication
import org.n1.mainframe.backend.util.createId
import org.springframework.http.server.ServerHttpRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.lang.RuntimeException
import java.security.Principal

class AssignPrincipalHandshakeHandler : DefaultHandshakeHandler() {

    override fun determineUser(request: ServerHttpRequest, wsHandler: WebSocketHandler?,
                               attributes: Map<String, Any>?): Principal {

        val authentication = SecurityContextHolder.getContext().authentication as? UserAuthentication ?: throw RuntimeException("Login please")

        val clientId = createId("client" )
        return ClientPrincipal(authentication.user, clientId)
    }

}