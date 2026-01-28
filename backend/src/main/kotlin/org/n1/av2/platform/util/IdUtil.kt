package org.n1.av2.platform.util

import org.n1.av2.site.entity.Node
import java.util.*


fun createId(prefix: String, findExisting: (String)-> Any?): String {
    return createIdGeneric("$prefix-", findExisting, 9, 18)
}

fun createLayerId(node: Node, findExisting: (String)-> Any?): String {
    return createIdGeneric("${node.id}:layer-", findExisting, 9, 13)
}

fun nodeIdFromLayerId(layerId: String): String {
    return layerId.substring(0,14)
}

fun createIdGeneric(prefix: String, findExisting: (String)-> Any?, start: Int = 9, end: Int = 18): String {
    var count = 0
    var id: String
    do {
        val uuidPart = UUID.randomUUID().toString().substring(start, end)
        id = "$prefix$uuidPart"
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

