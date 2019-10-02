package org.n1.av2.backend.engine

import org.n1.av2.backend.model.db.user.HackerIcon
import org.n1.av2.backend.model.db.user.User
import org.n1.av2.backend.model.iam.UserPrincipal
import java.util.concurrent.LinkedBlockingQueue

const val SLEEP_MILLIS_NO_EVENTS = 20L


class TimedEventRunner(
        private val timedEventQueue: TimedEventQueue,
        private val queue: LinkedBlockingQueue<Task>,
        private val gameEventService: GameEventService) : Runnable {


    private val systemPrincipal = UserPrincipal(User(id = "user-system", name = "System", icon = HackerIcon.BEAR))

    override fun run() {
        while(true) {
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

        return Task(action, systemPrincipal)

    }
}
