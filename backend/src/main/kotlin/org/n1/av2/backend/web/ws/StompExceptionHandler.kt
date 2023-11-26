package org.n1.av2.backend.web.ws

import jakarta.validation.ConstraintViolationException
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ReduxEvent
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.web.bind.annotation.ControllerAdvice


@ControllerAdvice
class StompExceptionHandler(
    private val stompService: StompService,
    private val currentUserService: CurrentUserService,
) {

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: ConstraintViolationException): Any? {
        return if (!currentUserService.isSystemUser) {
            stompService.replyError(exception.message!!)
            null
        } else {
            ReduxEvent(ServerActions.SERVER_NOTIFICATION, NotyMessage(
                title= "Invalid input",
                type= NotyType.NEUTRAL,
                message= exception.message!!
            ))
        }
    }
}