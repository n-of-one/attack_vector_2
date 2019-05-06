package org.n1.mainframe.backend.config

import mu.KLogging
import org.n1.mainframe.backend.service.user.HackerActivityService
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
class StompConfig(
        val hackerActivityService: HackerActivityService
) : WebSocketMessageBrokerConfigurer {

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
        registration!!.interceptors(StompMessageLogInterceptor(hackerActivityService))
    }

    @EventListener
    fun handleSubscribeEvent(event: SessionSubscribeEvent) {
        logger.info{ "<==> handleSubscribeEvent: username=" + event.user!!.name + ", event=" + event }
    }

    @EventListener
    fun handleConnectEvent(event: SessionConnectEvent) {
        hackerActivityService.startActivityOnline(event.user!!)
        logger.info{ "===> handleConnectEvent: username=" + event.user!!.name + ", event=" + event }
    }

    @EventListener
    fun handleDisconnectEvent(event: SessionDisconnectEvent) {
        hackerActivityService.endActivity(event.user!!)
        logger.info{ "<=== handleDisconnectEvent: username=" + event.user!!.name + ", event=" + event }
    }
}