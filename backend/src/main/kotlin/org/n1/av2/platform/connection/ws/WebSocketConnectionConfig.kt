package org.n1.av2.platform.connection.ws

import org.n1.av2.platform.connection.*
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.NOT_LOGGED_IN_USER
import org.n1.av2.platform.iam.user.ROLE_USER
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.http.server.ServerHttpRequest
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer
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


@Configuration
class WebSocketSecurityConfig: AbstractSecurityWebSocketMessageBrokerConfigurer() {


    override fun sameOriginDisabled(): Boolean {
        return true
    }
}

@Configuration
@EnableWebSocketMessageBroker
@Component
class WebSocketConfig(
    val connectDisconnectService: ConnectDisconnectService,
    private val connectionIdService: ConnectionIdService,
) : WebSocketMessageBrokerConfigurer {

    private val logger = mu.KotlinLogging.logger {}


    override fun registerStompEndpoints(registry: StompEndpointRegistry) {

        fun determineUser(notLoggedInUserName: String ): Principal {
            // If the user is logged in, the UserPrincipal has already been set when the http-request passed through the JwtAuthentication filter. The JWT was taken from the cookie.
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication !is UserPrincipal || !authentication.userEntity.type.authorities.contains(ROLE_USER)) {
                // signal to the client that they need to log in first.
                return UserPrincipal(
                    _name = notLoggedInUserName,
                    connectionId = connectionIdService.create(),
                    userEntity = NOT_LOGGED_IN_USER,
                    type = ConnectionType.NONE,
                )
            }

            /// Replace the name with username + connectionId, so that the Client will know its unique connectionId.
            val userPrincipal: UserPrincipal = authentication
            return userPrincipal.copy(_name = "${userPrincipal.userId}_${userPrincipal.connectionId}")
        }

        class AuthenticatedHandshakeHandler : DefaultHandshakeHandler() {
            override fun determineUser(
                request: ServerHttpRequest, wsHandler: WebSocketHandler,
                attributes: Map<String, Any>
            ): Principal {
                return determineUser("login-needed") // this username will trigger the frontend to redirect to the login page.
            }
        }

        class UnrestrictedHandshakeHandler : DefaultHandshakeHandler() {
            override fun determineUser(
                request: ServerHttpRequest, wsHandler: WebSocketHandler,
                attributes: Map<String, Any>
            ): Principal {
                return determineUser("none") // this username will not trigger a redirect to login.
            }
        }

        registry
            .addEndpoint(UNRESTRICTED_ENDPOINT)
            .setAllowedOrigins("*")
            .setHandshakeHandler(UnrestrictedHandshakeHandler())

        registry
            .addEndpoint(HACKER_ENDPOINT)
            .setAllowedOrigins("*")
            .setHandshakeHandler(AuthenticatedHandshakeHandler())

        registry
            .addEndpoint(NETWORKED_APP_ENDPOINT)
            .setAllowedOrigins("*")
            .setHandshakeHandler(AuthenticatedHandshakeHandler())

        registry.setPreserveReceiveOrder(true)

    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/av")
        registry.enableSimpleBroker("/topic", "/reply", "/error")
        registry.setUserDestinationPrefix("/user")
        registry.setPreservePublishOrder(true)

    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(MessageIn())
    }

    @EventListener
    fun handleConnectEvent(event: SessionConnectEvent) {
        try {
            // A bit hacky: need to set the security context for the stompConnectionEventService to be able to do its thing
            // but because we are not formally connected yet.
            val userPrincipal = event.user!! as UserPrincipal
            SecurityContextHolder.getContext().authentication = userPrincipal
            logger.debug { "<= connect ${userPrincipal.name}" }

            connectDisconnectService.connect(userPrincipal)
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @EventListener
    fun handleDisconnectEvent(event: SessionDisconnectEvent) {
        val userPrincipal = event.user!! as UserPrincipal
        connectDisconnectService.browserDisconnect(userPrincipal)
        logger.debug { "<= disconnect ${userPrincipal.name}" }
    }

    @EventListener
    fun handleSubscribeEvent(event: SessionSubscribeEvent) {
        logger.debug { "<= subscribe ${event.user!!.name} ${event.message.headers["nativeHeaders"]}" }
    }
}
