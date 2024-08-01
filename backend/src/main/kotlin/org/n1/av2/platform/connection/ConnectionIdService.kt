package org.n1.av2.platform.connection

import org.n1.av2.platform.util.createId
import org.springframework.stereotype.Service

@Service
class ConnectionIdService {

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
