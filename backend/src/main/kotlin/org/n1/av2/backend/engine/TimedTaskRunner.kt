package org.n1.av2.backend.engine

import org.n1.av2.backend.model.iam.UserPrincipal
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.concurrent.withLock

const val SLEEP_MILLIS_NO_EVENTS = 20L
const val TICK_MILLIS = 50
const val SECOND_MILLIS = 1000
const val SECONDS_IN_TICKS = SECOND_MILLIS / TICK_MILLIS


data class TaskIdentifiers(val userId: String?, val siteId: String?, val layerId: String?)

/**
 * Tasks can be scheduled to run in the future. Then will be held here and when their time comes,
 * added to the TaskRunner to be executed.
 */
private class TimedTask(
    val due: Long,
    val userPrincipal: UserPrincipal,
    val action: () -> Unit,
    val identifiers: TaskIdentifiers,
)


@Component
class TimedTaskRunner(
    private val taskEngine: TaskEngine
) : Runnable {

    private val logger = mu.KotlinLogging.logger {}

    private var running = true
    private val timedTasks = LinkedList<TimedTask>()
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
            if (timedTasks.isEmpty()) {
                return SLEEP_MILLIS_NO_EVENTS
            }

            val now = System.currentTimeMillis()
            val due = timedTasks.first.due
            if (now < due) {
                return SLEEP_MILLIS_NO_EVENTS
            }

            val event = timedTasks.removeAt(0)
            taskEngine.runForUser(event.userPrincipal, event.action)
            return 0
        }
    }

    fun add(identifiers: TaskIdentifiers, due: Long, userPrincipal: UserPrincipal, action: () -> Unit) {
        lock.withLock {
            val task = TimedTask(due, userPrincipal, action, identifiers)
            timedTasks.add(task)
            timedTasks.sortBy { it.due }
        }
    }

    fun removeAllForUser(userId: String) {
        val identifiers = TaskIdentifiers(userId, null, null)
        removeAll(identifiers)
    }

    fun removeAll(identifiers: TaskIdentifiers) {
        lock.withLock {
            println("Before")
            timedTasks.forEach { task ->
                println( "identifiers: ${task.identifiers} ${task.due}")
            }

            timedTasks.removeIf {task ->
                task.identifiers.userId == identifiers.userId ||
                task.identifiers.siteId == identifiers.siteId ||
                task.identifiers.layerId == identifiers.layerId
            }
            if (identifiers.userId != null) {
                taskEngine.removeForUser(identifiers.userId)
            }

            println("After")
            timedTasks.forEach { task ->
                println( "identifiers: ${task.identifiers} ${task.due}")
            }

        }
    }

    @PreDestroy
    fun terminate() {
        running = false
    }

}
