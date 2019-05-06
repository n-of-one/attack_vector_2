package org.n1.mainframe.backend.util

import org.n1.mainframe.backend.model.ui.NotyMessage
import java.time.LocalTime

fun formattedTime(): String {
    val time = LocalTime.now()
    return "%02d:%02d".format(time.hour, time.minute)
}

fun createNoty(exception: Exception): NotyMessage {
    val msg = NotyMessage("fatal", "Server error", exception.message ?: "")
    return msg
}
