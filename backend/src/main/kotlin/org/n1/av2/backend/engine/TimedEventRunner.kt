package org.n1.av2.backend.engine

import mu.KLogging
import org.n1.av2.backend.model.db.user.HackerIcon
import org.n1.av2.backend.model.db.user.User
import java.util.concurrent.LinkedBlockingQueue

const val SLEEP_MILLIS_NO_EVENTS = 20L

const val SYSTEM_USER_ID = "user-system"

val systemUser = User(id = SYSTEM_USER_ID, name = "System", icon = HackerIcon.BEAR)


class TimedEventRunner(
        private val timedEventQueue: TimedEventQueue,
        private val queue: LinkedBlockingQueue<Task>,
        private val gameEventService: GameEventService) : Runnable {

    companion object : KLogging()

    private var running = true

    override fun run() {
        while(running) {
            processEvent()
        }
    }

    private fun processEvent() {
        val due = timedEventQueue.nextDue() ?: System.currentTimeMillis() + SLEEP_MILLIS_NO_EVENTS
        val now = System.currentTimeMillis()
        if (now >= due) {
            val timedEvent = timedEventQueue.nextEvent() ?: return
            val task = createTimedTask(timedEvent)
            queue.add(task)
        }
        else {
            val sleepTime = Math.min(SLEEP_MILLIS_NO_EVENTS, due - now)
            Thread.sleep(sleepTime)
        }
    }

    private fun createTimedTask(timedEvent: TimedEvent): Task {
        val action: () -> Unit = {
            logger.debug("== ${timedEvent.omniId} ${timedEvent.event.javaClass.simpleName}")
            val gameEvent = timedEvent.event
            gameEventService.run(gameEvent)
        }

        return Task(action, systemUser)

    }

    fun terminate() {
        running = false
    }
}
