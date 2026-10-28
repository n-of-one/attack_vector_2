package org.n1.av2.platform.connection.ws

import io.github.oshai.kotlinlogging.KotlinLogging
import org.n1.av2.frontend.model.ReduxEvent
import org.n1.av2.platform.util.toServerFatalReduxEvent
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller

@Controller
class WebSocketExceptionHandlingController(
) {
    private val logger = KotlinLogging.logger {}

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: Exception): ReduxEvent {
        logger.error(exception) { exception.message }
        return toServerFatalReduxEvent(exception)
    }
}
