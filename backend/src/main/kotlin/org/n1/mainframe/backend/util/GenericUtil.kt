package org.n1.mainframe.backend.util

import org.n1.mainframe.backend.model.ui.ReduxEvent
import org.n1.mainframe.backend.service.ReduxActions

data class ServerFatal(val recoverable: Boolean, val message: String )

fun toServerFatalReduxEvent(exception: Exception): ReduxEvent {
    val recoverable = exception !is FatalException
    val event = ServerFatal(recoverable, exception.message ?: "-")
    return ReduxEvent(ReduxActions.SERVER_ERROR, event)
}
