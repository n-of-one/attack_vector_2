package org.n1.av2.backend.engine

import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

const val FRAME_MILLIS = 20

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

    fun queueInFrames(frames: Int, event: GameEvent) {
        val due = System.currentTimeMillis() + FRAME_MILLIS * frames
        add(due, event)
    }

    fun queueInSeconds(seconds: Int, event: GameEvent) {
        val due = System.currentTimeMillis() + 1000 * seconds
        add(due, event)
    }

    fun queueInMinutesAndSeconds(minutes: Int, seconds: Int, event: GameEvent) {
        val due = System.currentTimeMillis() + 1000 * seconds + 60000 * minutes
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