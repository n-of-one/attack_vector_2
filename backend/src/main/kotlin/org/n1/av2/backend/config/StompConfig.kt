package org.n1.av2.backend.config

import mu.KLogging
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.user.HackerActivityService
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


@Configuration
@EnableWebSocketMessageBroker
@Component
class StompConfig(val hackerActivityService: HackerActivityService) : WebSocketMessageBrokerConfigurer {

    companion object: KLogging()

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
                .addEndpoint("/attack_vector_websocket")
                .setAllowedOrigins("*")
                .setHandshakeHandler(AssignPrincipalHandshakeHandler())
                .addInterceptors(CsrfTokenHandshakeInterceptor())
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/av")
        registry.enableSimpleBroker("/topic", "/reply", "/error")
        registry.setUserDestinationPrefix("/user")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration?) {
        registration!!.interceptors(StompMessageLogInterceptor())
    }

    @EventListener
    fun handleSubscribeEvent(event: SessionSubscribeEvent) {
        logger.debug{ "<==> handleSubscribeEvent: connection=${event.user!!.name} ${event.message.headers["nativeHeaders"]}" }
    }

    @EventListener
    fun handleConnectEvent(event: SessionConnectEvent) {
            hackerActivityService.connect(event.user!! as UserPrincipal)
            logger.debug{ "===> handleConnectEvent: connection=${event.user!!.name}" }
    }

    @EventListener
    fun handleDisconnectEvent(event: SessionDisconnectEvent) {
        hackerActivityService.disconnect(event.user!! as UserPrincipal)
        logger.debug{ "<=== handleDisconnectEvent: connection=${event.user!!.name}" }
    }
}