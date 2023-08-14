package org.n1.av2.backend.config.websocket

import mu.KLogger
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.support.ChannelInterceptor
import java.nio.charset.StandardCharsets.UTF_8
import java.security.Principal


class MessageIn : ChannelInterceptor {

    private val logger = mu.KotlinLogging.logger {}

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        if (message.headers["stompCommand"] == null) {
            return message
        }

        val command = message.headers["stompCommand"] as StompCommand
        if ("SEND" == command.name && logMessage(message)) {
            log(logger, message, "<-")
        }
        return message
    }

    private fun logMessage(message: Message<*>): Boolean {
        return message.headers["simpDestination"] != "/hb"
    }

}

class MessageOut : ChannelInterceptor {

    private val logger = mu.KotlinLogging.logger {}

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        log(logger, message, "->")
        return super.preSend(message, channel)
    }

}

private fun log(logger: KLogger, message: Message<*>, directionText: String) {
    val payload = message.payload as ByteArray
    val payloadString = String(payload, UTF_8)
    val destination = message.headers["simpDestination"]

    val user = if (message.headers["simpUser"] != null) {
        (message.headers["simpUser"] as Principal).name
    } else {
        "no name"
    }
    logger.debug("${directionText} ${user} ${destination} ${payloadString}")
}
