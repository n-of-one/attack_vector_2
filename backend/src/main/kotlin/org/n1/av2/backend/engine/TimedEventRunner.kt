package org.n1.av2.backend.engine

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
            val event = timedEventQueue.nextEvent() ?: return
            val task = createTimedTask(event)
            queue.add(task)
        }
        else {
            val sleepTime = Math.min(SLEEP_MILLIS_NO_EVENTS, due - now)
            Thread.sleep(sleepTime)
        }

    }

    private fun createTimedTask(event: GameEvent): Task {
        val action: () -> Unit = {
            gameEventService.run(event)
        }

        return Task(action, systemUser)

    }

    fun terminate() {
        running = false
    }
}
