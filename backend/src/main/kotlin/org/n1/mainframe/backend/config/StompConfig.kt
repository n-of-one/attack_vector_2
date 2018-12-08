package org.n1.mainframe.backend.config

import mu.KLogging
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import org.springframework.messaging.simp.config.ChannelRegistration





@Configuration
@EnableWebSocketMessageBroker
class StompConfig() : AbstractWebSocketMessageBrokerConfigurer() {

    companion object: KLogging()

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
//        registry.addEndpoint("/ws")
        registry
                .addEndpoint("/ws")
                .setAllowedOrigins("*")
                .setHandshakeHandler(AssignPrincipalHandshakeHandler())
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/app")
        registry.enableSimpleBroker("/topic", "/reply", "/error")
        registry.setUserDestinationPrefix("/user")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration?) {
        registration!!.interceptors(StompMessageLogInterceptor())
    }

    @EventListener
    fun handleSubscribeEvent(event: SessionSubscribeEvent) {
        logger.info{ "<==> handleSubscribeEvent: username=" + event.user.name + ", event=" + event }
    }

    @EventListener
    fun handleConnectEvent(event: SessionConnectEvent) {
        logger.info{ "===> handleConnectEvent: username=" + event.user.name + ", event=" + event }
    }

    @EventListener
    fun handleDisconnectEvent(event: SessionDisconnectEvent) {
        logger.info{ "<=== handleDisconnectEvent: username=" + event.user.name + ", event=" + event }
    }
}