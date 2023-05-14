package org.n1.av2.backend.web.ws

import jakarta.validation.ConstraintViolationException
import org.n1.av2.backend.service.StompService
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.web.bind.annotation.ControllerAdvice


@ControllerAdvice
class StompExceptionHandler(
    private val stompService: StompService
) {

    @MessageExceptionHandler
    fun handleException(exception: ConstraintViolationException) {
        stompService.replyError(exception.message!!)
    }
}