package org.n1.av2.backend.util

import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.model.ui.ReduxEvent

data class ServerFatal(val recoverable: Boolean, val message: String )

fun toServerFatalReduxEvent(exception: Exception): ReduxEvent {
    val recoverable = exception !is FatalException
    val event = ServerFatal(recoverable, exception.message ?: "-")
    return ReduxEvent(ReduxActions.SERVER_ERROR, event)
}
