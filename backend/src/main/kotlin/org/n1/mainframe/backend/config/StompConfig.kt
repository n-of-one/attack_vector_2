package org.n1.mainframe.backend.config

import mu.KLogging
import org.n1.mainframe.backend.model.iam.UserPrincipal
import org.n1.mainframe.backend.service.user.HackerActivityService
import org.n1.mainframe.backend.util.FatalException
import org.springframework.beans.factory.annotation.Autowired
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
class StompConfig() : WebSocketMessageBrokerConfigurer {

    companion object: KLogging()

    /** Prevent circular dependency */
    @Autowired
    lateinit var hackerActivityService: HackerActivityService


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
        try {
            hackerActivityService.startActivityOnline(event.user!! as UserPrincipal)
            logger.debug{ "===> handleConnectEvent: connection=${event.user!!.name}" }
        }
        catch(ignore: FatalException) {
            logger.info("=!=> handleConnectEvent: username=${event.user!!.name} - duplicate session detected for user, will close momentarily.")
        }
    }

    @EventListener
    fun handleDisconnectEvent(event: SessionDisconnectEvent) {
        hackerActivityService.endActivity(event.user!! as UserPrincipal)
        logger.debug{ "<=== handleDisconnectEvent: connection=${event.user!!.name}" }
    }
}