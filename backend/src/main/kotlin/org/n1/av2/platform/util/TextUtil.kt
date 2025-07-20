package org.n1.av2.platform.util

fun String.s(number: Int): String {
    return when(number) {
        0 -> "${this}s"
        1 -> this
        else -> "${this}s"
    }
}

fun pluralS(number: Int): String {
    return pluralS(number.toLong())
}

fun pluralS(list: List<Any>): String {
    return pluralS(list.size)
}

fun pluralS(number: Long): String {
    return when(number) {
        0L -> "s"
        1L -> ""
        else -> "s"
    }
}
