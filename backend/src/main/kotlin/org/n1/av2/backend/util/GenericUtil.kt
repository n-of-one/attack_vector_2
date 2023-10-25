package org.n1.av2.backend.util

import org.n1.av2.backend.model.ui.ReduxEvent
import org.n1.av2.backend.model.ui.ServerActions

data class ServerFatal(val recoverable: Boolean, val message: String )

fun toServerFatalReduxEvent(exception: Exception): ReduxEvent {
    val recoverable = exception !is FatalException
    val event = ServerFatal(recoverable, exception.message ?: "-")
    return ReduxEvent(ServerActions.SERVER_ERROR, event)
}

fun <T> T?.isOneOf(vararg candidates: T) : Boolean {
    return candidates.contains(this)
}
