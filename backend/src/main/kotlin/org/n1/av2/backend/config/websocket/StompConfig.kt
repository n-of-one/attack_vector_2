package org.n1.av2.backend.config.websocket

import org.n1.av2.backend.entity.user.User
import org.n1.av2.backend.entity.user.UserType
import org.n1.av2.backend.model.iam.UserPrincipal
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.http.server.ServerHttpRequest
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal
import java.util.*


@Configuration
@EnableWebSocketMessageBroker
@Component
class StompConfig(
    val stompConnectionEventService: StompConnectionEventService,
    private val connectionUtil: ConnectionUtil,
) : WebSocketMessageBrokerConfigurer {

    private val logger = mu.KotlinLogging.logger {}


    override fun registerStompEndpoints(registry: StompEndpointRegistry) {

        class UnauthenticatedHandshakeHandler : DefaultHandshakeHandler() {
            override fun determineUser(
                request: ServerHttpRequest, wsHandler: WebSocketHandler,
                attributes: Map<String, Any>
            ): Principal {
                val user = User(UUID.randomUUID().toString(), "", "","", UserType.NOT_LOGGED_IN, null, "")
                val connectionId = connectionUtil.create()
                val name = "${user.name}:${connectionId}"
                return UserPrincipal(name, connectionId, user, ConnectionType.WS_UNRESTRICTED)
            }
        }
        registry
            .addEndpoint(UNRESTRICTED_ENDPOINT)
            .setAllowedOrigins("*")
            .setHandshakeHandler(UnauthenticatedHandshakeHandler())


        class AuthenticatedHandshakeHandler : DefaultHandshakeHandler() {
            override fun determineUser(
                request: ServerHttpRequest, wsHandler: WebSocketHandler,
                attributes: Map<String, Any>
            ): Principal {
                // The UserPrincipal has already been set when the http-request passed through the JwtAuthentication filter. The JWT was taken from the cookie.

                // WebSecurityConfig will make sure only authenticated users get here.
                val userPrincipal = SecurityContextHolder.getContext().authentication as UserPrincipal
                /// Replace the name with username + connectionId, so that the Client will know its unique connectionId.
                return userPrincipal.copy(_name = "${userPrincipal.userId}:${userPrincipal.connectionId}")
            }
        }

        registry
            .addEndpoint(HACKER_ENDPOINT)
            .setAllowedOrigins("*")
            .setHandshakeHandler(AuthenticatedHandshakeHandler())

        registry
            .addEndpoint(NETWORKED_APP_ENDPOINT)
            .setAllowedOrigins("*")
            .setHandshakeHandler(AuthenticatedHandshakeHandler())

    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/av")
        registry.enableSimpleBroker("/topic", "/reply", "/error")
        registry.setUserDestinationPrefix("/user")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(MessageIn())
    }

    @EventListener
    fun handleConnectEvent(event: SessionConnectEvent) {
        // A bit hacky: need to set the security context for the stompConnectionEventService to be able to do its thing
        // but because we are not formally connected yet.
        try {
            val userPrincipal = event.user!! as UserPrincipal

            logger.debug { "<= connect ${userPrincipal.name}" }
            SecurityContextHolder.getContext().authentication = userPrincipal
            stompConnectionEventService.connect(userPrincipal)
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @EventListener
    fun handleDisconnectEvent(event: SessionDisconnectEvent) {
        val userPrincipal = event.user!! as UserPrincipal
        stompConnectionEventService.disconnect(userPrincipal)
        logger.debug { "<= disconnect ${userPrincipal.name}" }
    }

    @EventListener
    fun handleSubscribeEvent(event: SessionSubscribeEvent) {
        logger.debug { "<= subscribe ${event.user!!.name} ${event.message.headers["nativeHeaders"]}" }
        val principal = event.user!! as UserPrincipal
        stompConnectionEventService.sendTime(principal)
    }
}
