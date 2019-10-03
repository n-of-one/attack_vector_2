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

    fun queueInTicks(ticks: Int, event: GameEvent) {
        val due = System.currentTimeMillis() + TICK_MILLIS * ticks
        add(due, event)
    }

    fun queueInSeconds(seconds: Int, event: GameEvent) {
        val due = System.currentTimeMillis() + SECOND_MILLIS * seconds
        add(due, event)
    }

    fun queueInMinutesAndSeconds(minutes: Int, seconds: Int, event: GameEvent) {
        val due = System.currentTimeMillis() + SECOND_MILLIS * seconds + MINUTE_MILLIS * minutes
        add(due, event)
    }

    fun add(due: Long, event: GameEvent) {
        lock.withLock {
            events.add(TimedEvent(due, event))
            events.sortBy { it.due }
        }
    }

    fun nextEvent(): GameEvent? {
        lock.withLock {
            if (events.isEmpty()) {
                return null
            }
            return events.removeAt(0).event
        }
    }

}