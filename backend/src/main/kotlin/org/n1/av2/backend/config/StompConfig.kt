package org.n1.av2.backend.config

import mu.KLogging
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.StompService
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
import javax.annotation.PostConstruct


@Configuration
@EnableWebSocketMessageBroker
@Component
class StompConfig(val hackerActivityService: HackerActivityService) : WebSocketMessageBrokerConfigurer {

    companion object : KLogging()

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
        registration!!.interceptors(MessageIn())
    }

    // Have the StompService log this, as we get better info there: user-ids and run-ids instead of stomp channel ids.
//    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
//        registration.interceptors(MessageOut())
//    }

    @EventListener
    fun handleSubscribeEvent(event: SessionSubscribeEvent) {
        logger.debug { "<==> handleSubscribeEvent: connection=${event.user!!.name} ${event.message.headers["nativeHeaders"]}" }
        val principal = event.user!! as UserPrincipal
        hackerActivityService.sendTime(principal.name)
    }

    @EventListener
    fun handleConnectEvent(event: SessionConnectEvent) {
        val principal = event.user!! as UserPrincipal
        hackerActivityService.connect(principal)
        logger.debug { "===> handleConnectEvent: connection=${event.user!!.name}" }
    }

    @EventListener
    fun handleDisconnectEvent(event: SessionDisconnectEvent) {
        hackerActivityService.disconnect(event.user!! as UserPrincipal)
        logger.debug { "<=== handleDisconnectEvent: connection=${event.user!!.name}" }
    }
}

@Configuration
class ConfigStompServiceAndHackerActivityService(
        val stompService: StompService,
        val hackerActivityService: HackerActivityService) {

    @PostConstruct
    fun postConstruct() {
        hackerActivityService.stompService = stompService
    }

}
