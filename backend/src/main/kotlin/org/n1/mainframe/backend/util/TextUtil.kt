package org.n1.mainframe.backend.util

public fun String.s(number: Int): String {
    return when(number) {
        0 -> "${this}s"
        1 -> this
        else -> "${this}s"
    }
}
