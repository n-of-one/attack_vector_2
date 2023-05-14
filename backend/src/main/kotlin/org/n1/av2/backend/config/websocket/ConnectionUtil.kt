package org.n1.av2.backend.config.websocket

import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Component

@Component
class ConnectionUtil {

    val usedIds: MutableMap<String, Int> = HashMap()

    fun create(): String {
        val connectionId = createId("connection", ::findExisting)
        usedIds.put(connectionId, 1)
        return connectionId
    }

    fun recycle(connectionId: String) {
        usedIds.remove(connectionId)
    }

    fun findExisting(attempt: String): Int? {
        return usedIds[attempt]
    }

}