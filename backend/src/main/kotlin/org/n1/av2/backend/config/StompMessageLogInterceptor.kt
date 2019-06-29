package org.n1.av2.backend.config

import mu.KLogging
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets.UTF_8
import java.security.Principal


@Component
class StompMessageLogInterceptor : ChannelInterceptor {

    companion object : KLogging()

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val command = message.headers["stompCommand"] as StompCommand
        if ("SEND" == command.name) {
            val payload = message.payload as ByteArray
            val payloadString = String(payload, UTF_8)
            val destination = message.headers["simpDestination"]

            val user = if (message.headers["simpUser"] != null) {
                (message.headers["simpUser"] as Principal).name
            } else {
                "no name"
            }
            val millis = System.currentTimeMillis()
            logger.debug("${millis} ${user} ${destination} : ${payloadString}")
        }
        return message
    }
}