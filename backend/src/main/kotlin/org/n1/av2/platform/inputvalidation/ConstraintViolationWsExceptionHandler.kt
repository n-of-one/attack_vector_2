package org.n1.av2.platform.inputvalidation

import jakarta.validation.ConstraintViolationException
import org.n1.av2.frontend.model.NotyMessage
import org.n1.av2.frontend.model.NotyType
import org.n1.av2.frontend.model.ReduxEvent
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.user.CurrentUserService
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.web.bind.annotation.ControllerAdvice


@ControllerAdvice
class ConstraintViolationWsExceptionHandler(
    private val connectionService: ConnectionService,
    private val currentUserService: CurrentUserService,
) {

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: ConstraintViolationException): Any? {
        return if (!currentUserService.isSystemUser) {
            connectionService.replyError(exception.message!!)
            null
        } else {
            ReduxEvent(
                ServerActions.SERVER_NOTIFICATION, NotyMessage(
                title= "Invalid input",
                type= NotyType.NEUTRAL,
                message= exception.message!!
            )
            )
        }
    }
}
