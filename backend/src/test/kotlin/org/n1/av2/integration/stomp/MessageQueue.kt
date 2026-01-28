package org.n1.av2.integration.stomp

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MessageQueue {

    private val lock = ReentrantLock()

    private val queue = mutableListOf<ReceivedMessage>()

    fun add(message: ReceivedMessage) = lock.withLock {
        queue.add(message)
    }

    fun deleteIfContains(expected: ReceivedMessage): String? = lock.withLock {
        val message = queue.find { it.type == expected.type && it.payload.contains(expected.payload) }
        if (message != null) {
            queue.remove(message)
            return extractData(message)
        }
        return null
    }

    private fun extractData(message: ReceivedMessage): String {
        val start = message.payload.substringAfter("data\":")
        return start.substringBeforeLast("}")
    }

    fun logMessage() = lock.withLock {
        queue.forEach { message ->

            System.err.println("Message: ${message.type} : ${message.payload}")

        }
    }

    fun clear() = lock.withLock {
        queue.clear()
    }
}
