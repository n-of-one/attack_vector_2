package org.n1.av2.backend.config.websocket

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.user.UserConnectionService
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
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
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal
import javax.annotation.PostConstruct

const val MAIN_ENDPOINT = "/av_ws"
const val ICE_ENDPOINT = "/ice_ws"

@Configuration
@EnableWebSocketMessageBroker
@Component
class StompConfig(
    val stompConnectionEventService: StompConnectionEventService,
) : WebSocketMessageBrokerConfigurer {

    private val logger = mu.KotlinLogging.logger {}


    override fun registerStompEndpoints(registry: StompEndpointRegistry) {

        class WebSocketAuthenticationHandler(
            private val stompConnectionEventService: StompConnectionEventService
        ) : DefaultHandshakeHandler() {

            override fun determineUser(
                request: ServerHttpRequest, wsHandler: WebSocketHandler,
                attributes: Map<String, Any>
            ): Principal {

//                val httpRequest = (request as ServletServerHttpRequest).servletRequest
//                val path = httpRequest.requestURI


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

        registry
            .addEndpoint(MAIN_ENDPOINT)
            .setAllowedOrigins("*")
            .addInterceptors(MyHandshakeInterceptor())
            .setHandshakeHandler(WebSocketAuthenticationHandler(stompConnectionEventService))

        registry
            .addEndpoint(ICE_ENDPOINT)
            .setAllowedOrigins("*")
            .addInterceptors(MyHandshakeInterceptor())
            .setHandshakeHandler(WebSocketAuthenticationHandler(stompConnectionEventService))

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
        val userPrincipal = event.user!! as UserPrincipal

        val securityContextUserPrincipal = SecurityContextHolder.getContext().authentication

        // A bit hacky: need to set the security context for the stompConnectionEventService to be able to do its thing
        // but because we are not formally connected yet.
        try {
//            if (principal.user.type == UserType.HACKER) {
                SecurityContextHolder.getContext().authentication = userPrincipal
                stompConnectionEventService.connect(userPrincipal)
//            }
        }
        finally {
            SecurityContextHolder.clearContext()
        }
        logger.debug { "<= connect ${userPrincipal.name}" }
    }

    @EventListener
    fun handleDisconnectEvent(event: SessionDisconnectEvent) {
//        val message: Message<ByteArray> = event.message
//        val headers: MessageHeaders = message.headers
//        val attributes = headers["simpSessionAttributes"] ?: error("session attributes not found")
//        if (attributes !is Map<*, *>) error("wrong type of a:")
//        val path = attributes["path"]


       val userPrincipal = event.user!! as UserPrincipal
//        if (!userPrincipal.invalidated) {
            stompConnectionEventService.disconnect(userPrincipal)
            logger.debug { "<= disconnect ${userPrincipal.name}" }
//        }

    }

    @EventListener
    fun handleSubscribeEvent(event: SessionSubscribeEvent) {
        logger.debug { "<= subscribe ${event.user!!.name} ${event.message.headers["nativeHeaders"]}" }
        val principal = event.user!! as UserPrincipal
        stompConnectionEventService.sendTime(principal)
    }
}



/// Prevent circular connection
@Configuration
class ConfigureStompServiceAndHackerActivityService(
    val taskRunner: TaskRunner,
    val userConnectionService: UserConnectionService,
    val stompConnectionEventService: StompConnectionEventService
) {

    @PostConstruct
    fun postConstruct() {
        stompConnectionEventService.taskRunner = taskRunner
        stompConnectionEventService.userConnectionService = userConnectionService
    }

}

class MyHandshakeInterceptor : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val httpRequest = (request as ServletServerHttpRequest).servletRequest
        attributes["path"] = httpRequest.requestURI
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
    }
}
