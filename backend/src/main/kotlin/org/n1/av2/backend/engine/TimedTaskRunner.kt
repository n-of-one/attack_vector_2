package org.n1.av2.backend.engine

import mu.KLogging
import org.n1.av2.backend.model.db.user.User
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.concurrent.withLock

const val SLEEP_MILLIS_NO_EVENTS = 20L
const val TICK_MILLIS = 50
const val SECOND_MILLIS = 1000
const val MINUTE_MILLIS = 60 * SECOND_MILLIS

const val SYSTEM_USER_ID = "user-system"

/**
 * Tasks can be scheduled to run in the future. Then will be held here and when their time comes,
 * added to the TaskRunner to be executed.
 */
private class TimedEvent(val omniId: String, val due: Long, val user: User, val action: () -> Unit)

@Component
class TimedTaskRunner(
    private val taskEngine: TaskEngine
) : Runnable {

    companion object : KLogging()

    private var running = true
    private val timedTasks = LinkedList<TimedEvent>()
    private val lock = ReentrantLock()

    @PostConstruct
    fun postConstruct() {
        Thread(this).start()
    }

    override fun run() {
        while (running) {
            val sleepMillis = processEvent()
            Thread.sleep(sleepMillis)
        }
    }

    private fun processEvent(): Long {
        lock.withLock {
            if (timedTasks.isEmpty()) { return SLEEP_MILLIS_NO_EVENTS }

            val now = System.currentTimeMillis()
            val due = timedTasks.first.due
            if (now < due) { return SLEEP_MILLIS_NO_EVENTS }

            val event = timedTasks.removeAt(0)
            taskEngine.runForUser(event.user, event.action)
            return 0
        }
    }

    fun add(omniId: String, due: Long, user: User, action: () -> Unit) {
        lock.withLock {
            timedTasks.add(TimedEvent(omniId, due, user, action))
            timedTasks.sortBy { it.due }
        }
    }

    fun removeAllFor(omniId: String) {
        timedTasks.removeIf { it.omniId == omniId }
    }

    @PreDestroy
    fun terminate() {
        running = false
    }


}
