package org.n1.av2.backend.config

import mu.KLogger
import mu.KLogging
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.support.ChannelInterceptor
import java.nio.charset.StandardCharsets.UTF_8
import java.security.Principal


class MessageIn : ChannelInterceptor {
    companion object : KLogging()

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        if (message.headers["stompCommand"] == null) {
            return message
        }

        val command = message.headers["stompCommand"] as StompCommand
        if ("SEND" == command.name) {
            log(logger, message, "<-")
        }
        return message
    }
}

class MessageOut : ChannelInterceptor {
    companion object : KLogging()

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        log(MessageIn.logger, message, "->")
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
