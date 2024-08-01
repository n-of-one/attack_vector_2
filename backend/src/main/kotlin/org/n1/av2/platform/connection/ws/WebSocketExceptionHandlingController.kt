package org.n1.av2.platform.connection.ws

import org.n1.av2.frontend.model.ReduxEvent
import org.n1.av2.platform.util.toServerFatalReduxEvent
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller

@Controller
class WebSocketExceptionHandlingController(
) {
    private val logger = mu.KotlinLogging.logger {}

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: Exception): ReduxEvent {
        logger.error(exception.message, exception)
        return toServerFatalReduxEvent(exception)
    }
}
