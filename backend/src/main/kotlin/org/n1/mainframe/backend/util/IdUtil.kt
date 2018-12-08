package org.n1.mainframe.backend.util

import java.util.*

fun createId(prefix: String, findExisting: (String)-> Any? ): String {
    var count = 0
    var id: String
    do {
        val uuidPart = UUID.randomUUID().toString().substring(9, 18)
        id = "$prefix-$uuidPart"
        val existing = findExisting(id)
        count ++
    }
    while (existing != null && count < 10)

    if (count == 10) {
        throw RuntimeException("Failed to generate unique id for prefix: ${prefix} ")
    }
    return id
}