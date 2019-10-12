package org.n1.av2.backend.config

import mu.KLogging
import org.n1.av2.backend.engine.SerializingExecutor
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.StompConnectionEventService
import org.n1.av2.backend.service.user.UserConnectionService
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.security.messaging.web.socket.server.CsrfTokenHandshakeInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import javax.annotation.PostConstruct


@Configuration
@EnableWebSocketMessageBroker
@Component
class StompConfig(
        val assignPrincipalHandshakeHandler: AssignPrincipalHandshakeHandler,
        val stompConnectionEventService: StompConnectionEventService) : WebSocketMessageBrokerConfigurer {

    companion object : KLogging()

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
                .addEndpoint("/attack_vector_websocket")
                .setAllowedOrigins("*")
                .setHandshakeHandler(assignPrincipalHandshakeHandler)
                .addInterceptors(CsrfTokenHandshakeInterceptor())
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/av")
        registry.enableSimpleBroker("/topic", "/reply", "/error")
        registry.setUserDestinationPrefix("/user")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration?) {
        registration!!.interceptors(MessageIn())
    }

    // Have the StompService log this, as we get better info there: user-ids and run-ids instead of stomp channel ids.
//    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
//        registration.interceptors(MessageOut())
//    }


    @EventListener
    fun handleConnectEvent(event: SessionConnectEvent) {
        val principal = event.user!! as UserPrincipal
//        val validConnection = stompConnectionEventService.connect(principal)
//        if (!validConnection) {
//            principal.invalidate()
//        }
        logger.debug { "<= connect ${event.user!!.name}" }
    }

    @EventListener
    fun handleDisconnectEvent(event: SessionDisconnectEvent) {
        val userPrincipal = event.user!! as UserPrincipal
        if (!userPrincipal.invalidated) {
            stompConnectionEventService.disconnect(userPrincipal)
            logger.debug { "<= disconnect ${userPrincipal.name}" }
        }

    }

    @EventListener
    fun handleSubscribeEvent(event: SessionSubscribeEvent) {
        logger.debug { "<= subscribe ${event.user!!.name} ${event.message.headers["nativeHeaders"]}" }
        val principal = event.user!! as UserPrincipal
        stompConnectionEventService.sendTime(principal)
    }
}

@Configuration
class ConfigureStompServiceAndHackerActivityService(
        val executor: SerializingExecutor,
        val userConnectionService: UserConnectionService,
        val stompConnectionEventService: StompConnectionEventService) {

    @PostConstruct
    fun postConstruct() {
        stompConnectionEventService.executor = executor
        stompConnectionEventService.userConnectionService = userConnectionService
    }

}
