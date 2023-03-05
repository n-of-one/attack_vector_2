package org.n1.av2.backend.config.security

import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.StompConnectionEventService
import org.springframework.http.server.ServerHttpRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

class WebSocketAuthenticationHandler(
    private val stompConnectionEventService: StompConnectionEventService
) : DefaultHandshakeHandler() {

    override fun determineUser(
        request: ServerHttpRequest, wsHandler: WebSocketHandler,
        attributes: Map<String, Any>
    ): Principal {

        /// The UserPrincipal has already been set when the http-request passed through the JwtAuthentication filter. The JWT was taken from the cookie.
        val userPrincipal = SecurityContextHolder.getContext().authentication as? UserPrincipal ?: throw RuntimeException("Login please")


//        if (principal.user.type == UserType.HACKER) {
//            val validConnection = stompConnectionEventService.connect(principal)
//            if (!validConnection) {
//                // Throwing an error here would cause the client to receive a 500. We want to do more: convey the problem.
//                // By invalidating the pricipal, it will return a user-name that indicates the problem. And it will allow us to treat this
//                // as an invalid connection from now on.
//                principal.invalidate()
//            }
//        }

        /// Replace the name with username + connectionId, so that the Client will know its unique connectionId.
        return userPrincipal.copy(_name = "${userPrincipal.userId}:${userPrincipal.connectionId}" )
    }
}