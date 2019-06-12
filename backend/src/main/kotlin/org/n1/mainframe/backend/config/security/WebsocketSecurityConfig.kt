package org.n1.mainframe.backend.config.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer

@Configuration
class WebSocketSecurityConfig: AbstractSecurityWebSocketMessageBrokerConfigurer() {

    override fun configureInbound(messages: MessageSecurityMetadataSourceRegistry ) {
//        messages.simpDestMatchers("/user/*").authenticated()
    }

    override fun sameOriginDisabled(): Boolean {
        return true
    }
}