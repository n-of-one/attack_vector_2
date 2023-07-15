package org.n1.av2.backend.util

import org.n1.av2.backend.model.ui.ReduxEvent
import org.n1.av2.backend.model.ui.ServerActions

data class ServerFatal(val recoverable: Boolean, val message: String )

fun toServerFatalReduxEvent(exception: Exception): ReduxEvent {
    val recoverable = exception !is FatalException
    val event = ServerFatal(recoverable, exception.message ?: "-")
    return ReduxEvent(ServerActions.SERVER_ERROR, event)
}

fun createPathWithQuery(path: String, params: Map<String, Any?>): String {
    if (params.isEmpty()) return path
    val query = params.filter { it.value != null }.map { "${it.key}=${it.value}" }.joinToString("&")
    return "$path?$query"
}