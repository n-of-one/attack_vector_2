package org.n1.av2.backend.engine

import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

const val TICK_MILLIS = 50
const val SECOND_MILLIS = 1000
const val MINUTE_MILLIS = 60 * SECOND_MILLIS

/**
 * Queue to hold times events.
 */
@Service
class TimedEventQueue {

    val events = LinkedList<TimedEvent>()
    val lock = ReentrantLock()

    fun nextDue(): Long? {
        lock.withLock {
            if (events.isEmpty()) {
                return null
            }
            return events.first.due
        }
    }

    fun queueInTicks(omniId: String, event: TicksGameEvent) {
        val due = System.currentTimeMillis() + TICK_MILLIS * event.ticks.total
        add(omniId, due, event)
    }

    fun queueInSeconds(omniId: String, seconds: Int, event: GameEvent) {
        val due = System.currentTimeMillis() + SECOND_MILLIS * seconds
        add(omniId, due, event)
    }

    fun queueInMinutesAndSeconds(omniId: String, minutes: Long, seconds: Long, event: GameEvent) {
        val due = System.currentTimeMillis() + SECOND_MILLIS * seconds + MINUTE_MILLIS * minutes
        add(omniId, due, event)
    }

    fun add(omniId: String, due: Long, event: GameEvent) {
        lock.withLock {
            events.add(TimedEvent(omniId, due, event))
            events.sortBy { it.due }
        }
    }

    fun nextEvent(): TimedEvent? {
        lock.withLock {
            if (events.isEmpty()) {
                return null
            }
            return events.removeAt(0)
        }
    }

    fun removeAllFor(omniId: String) {
        events.removeIf { it.omniId == omniId }
    }

}