package org.n1.av2.backend.util

public fun String.s(number: Int): String {
    return when(number) {
        0 -> "${this}s"
        1 -> this
        else -> "${this}s"
    }
}
