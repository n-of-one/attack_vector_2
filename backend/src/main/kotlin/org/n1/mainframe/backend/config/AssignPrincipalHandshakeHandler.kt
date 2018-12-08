package org.n1.mainframe.backend.config

import org.n1.mainframe.backend.util.createId
import org.springframework.http.server.ServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

class AssignPrincipalHandshakeHandler : DefaultHandshakeHandler() {

    fun rnull(input: String): Any? {
        return null
    }

    override fun determineUser(request: ServerHttpRequest, wsHandler: WebSocketHandler?,
                               attributes: Map<String, Any>?): Principal {

        val name = createId("client", ::rnull )
        return Principal { name }
    }

}