package org.n1.mainframe.backend.util

import org.apache.naming.SelectorContext.prefix
import java.util.*

fun createId(prefix: String, findExisting: (String)-> Any?): String {
    return createId(prefix, findExisting, 9, 18)
}

fun createServiceId(siteId: String, findExisting: (String)-> Any?): String {
    return createId("$siteId-serv", findExisting, 9, 13)
}

fun createId(prefix: String, findExisting: (String)-> Any?, start: Int, end: Int): String {
    var count = 0
    var id: String
    do {
        val uuidPart = UUID.randomUUID().toString().substring(start, end)
        id = "$prefix-$uuidPart"
        var existing = findExisting(id)
        if (existing is Optional<*> && !existing.isPresent) {
            existing = null
        }
        count ++
    }
    while (existing != null && count < 10)

    if (count == 10) {
        throw RuntimeException("Failed to generate unique id for prefix: ${prefix} ")
    }
    return id
}

