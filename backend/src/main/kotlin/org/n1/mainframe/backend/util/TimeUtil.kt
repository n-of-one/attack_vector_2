package org.n1.mainframe.backend.util

import java.time.LocalTime

fun formattedTime(): String {
    val time = LocalTime.now()
    return "%02d:%02d".format(time.hour, time.minute)
}
