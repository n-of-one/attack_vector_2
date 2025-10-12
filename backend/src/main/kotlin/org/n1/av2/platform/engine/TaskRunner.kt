package org.n1.av2.platform.engine

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.inputvalidation.ValidationException
import org.n1.av2.platform.util.FatalException
import org.n1.av2.platform.util.ServerFatal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.system.measureTimeMillis

const val TICK_MILLIS = 50
const val SECOND_MILLIS = 1000
const val SECONDS_IN_TICKS = SECOND_MILLIS / TICK_MILLIS

private class Task(val action: () -> Unit, val userPrincipal: UserPrincipal, val description: String)

data class TaskInfo(val inSeconds: Long, val dueAtLocalMillis: Long, val description: String)

/**
 * All actions are performed on a single thread, this way the execution becomes very predictable,
 * and we don't worry about parallel execution in the rest of our code.
 */
@Component
class UserTaskRunner(
    private val taskEngine: TaskEngine,
) {

    fun runTask(description: String, userPrincipal: UserPrincipal, action: () -> Unit) {
        taskEngine.runForUser(userPrincipal, action, description)
    }

    fun queueInTicksForSite(description: String, siteId: String, waitTicks: Int, action: () -> Unit) {
        val identifiers = mapOf("siteId" to siteId)
        queueInTicks(description, identifiers, waitTicks, action)
    }

    fun queueInTicks(description: String, identifiers: Map<String, String>, waitTicks: Int, action: () -> Unit) {
        val due = System.currentTimeMillis() + TICK_MILLIS * waitTicks
        val userPrincipal = SecurityContextHolder.getContext().authentication as UserPrincipal
        taskEngine.queueInMillis(description, identifiers, due, userPrincipal, action)
    }

    fun queue(description: String, identifiers: Map<String, String>, wait: Duration, action: () -> Unit) {
        val due = System.currentTimeMillis() + wait.toMillis()
        val userPrincipal = SecurityContextHolder.getContext().authentication as UserPrincipal
        taskEngine.queueInMillis(description, identifiers, due, userPrincipal, action)
    }

    /// run is ambiguous with Kotlin's extension function: run. So we implement it ourselves to prevent bugs
    @Deprecated(
        message = ">> Do not use run, Use runTask() instead",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("userTaskRunner.runTask(principal) {}")
    )
    @Suppress("unused")
    fun run(action: () -> Unit) {
        error(">> Do not use run, this is ambiguous with Kotlin run. Use runTask() instead")
    }

}


/**
 * All actions are performed on a single thread, this way the execution becomes very predictable,
 * and we don't worry about parallel execution in the rest of our code.
 */
@Component
class SystemTaskRunner(
    private val taskEngine: TaskEngine,
) {

    fun queueInTicks(description: String, identifiers: Map<String, String>, waitTicks: Int, action: () -> Unit) {
        val due = System.currentTimeMillis() + TICK_MILLIS * waitTicks
        taskEngine.queueInMillis(description, identifiers, due, UserPrincipal.system(), action)
    }

    fun queueInSeconds(description: String, identifiers: Map<String, String>, seconds: Long, action: () -> Unit) {
        val due = System.currentTimeMillis() + seconds * 1000
        taskEngine.queueInMillis(description, identifiers, due, UserPrincipal.system(), action)
    }

    fun removeTaskWithExactIdentifiers(identifiers: Map<String, String>): TaskInfo {
        return taskEngine.removeTaskWithExactIdentifiers(identifiers)
    }

    // run is ambiguous with Kotlin's extension function: run. So we implement it ourselves to prevent bugs
    @Deprecated(
        message = ">> Do not use run, Use runTask() instead",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("taskRunner.runTask(principal) {}")
    )
    @Suppress("unused")
    fun run(unused: () -> Unit) {
        error(">> Do not use run, this is ambiguous with Kotlin run. Use runTask() instead")
    }

}

@Component
class TaskEngine(
    val connectionService: ConnectionService,
    val currentUserService: CurrentUserService,
) : Runnable {

    private val logger = mu.KotlinLogging.logger {}

    private val timedTaskRunner = TimedTaskRunner()
    private val queue = LinkedBlockingQueue<Task>()
    private var running = true

    @PostConstruct
    fun init() {
        Thread(this).start()
    }

    fun runForUser(userPrincipal: UserPrincipal, action: () -> Unit, description: String) {
        val task = Task(action, userPrincipal, description)
        queue.put(task)
    }

    fun queueInMillis(description: String, identifiers: Map<String, String>, due: Long, userPrincipal: UserPrincipal, action: () -> Unit) {
        timedTaskRunner.queueInMillis(description, identifiers, due, userPrincipal, action)
    }

    override fun run() {
        while (running) {
            runTask()
            checkTimedTasks()
        }
    }

    fun checkTimedTasks() {
        val event = timedTaskRunner.getEvent() ?: return
        runForUser(event.userPrincipal, event.action, event.description)
    }

    private fun runTask() {
        val task = queue.poll(1, TimeUnit.MILLISECONDS) ?: return
        try {
            currentUserService.set(task.userPrincipal.userEntity)
            SecurityContextHolder.getContext().authentication = task.userPrincipal
            runAndLogTiming(task)
        } catch (exception: Exception) {
            if (exception is InterruptedException) {
                throw exception
            }
            if (exception is ValidationException) {
                connectionService.replyMessage(exception.getNoty())
                return
            }
            if (exception is FatalException) {
                val event = ServerFatal(false, exception.message ?: exception.javaClass.name)
                connectionService.reply(ServerActions.SERVER_ERROR, event)
                logger.warn("${task.userPrincipal.userEntity.name}: ${exception}")
                return
            }
            if (!currentUserService.isSystemUser) {
                val event = ServerFatal(true, exception.message ?: exception.javaClass.name)
                connectionService.reply(ServerActions.SERVER_ERROR, event)
                logger.info("User: ${task.userPrincipal.userEntity.name} - task triggered exception. Task: ${task.description}", exception)
            } else {
                logger.info("SYSTEM - task triggered exception. ", exception)
            }
        } catch (error: Throwable) {
            logger.error("Task triggered error.", error)
            val event = ServerFatal(false, error.message ?: error.javaClass.name)
            connectionService.reply(ServerActions.SERVER_ERROR, event)
            return
        } finally {
            currentUserService.remove()
            SecurityContextHolder.clearContext()
        }
    }

    private fun runAndLogTiming(task: Task) {
        val millis = measureTimeMillis {
            task.action()
        }
        logger.info("Task took ${millis}ms : ${task.description}")
    }

    @PreDestroy
    fun terminate() {
        this.running = false
    }

    fun removeForUser(userId: String) {
        this.queue.removeIf { it.userPrincipal.userId == userId }
    }

    fun removeAll(identifiers: Map<String, String>) {
        timedTaskRunner.removeAll(identifiers)
        val userIdIdentifier = identifiers["userId"]
        if (userIdIdentifier != null) {
            removeForUser(userIdIdentifier)
        }
    }

    fun sendTasks(userPrincipal: UserPrincipal) {
        @Suppress("unused")
        class TaskInfo(val due: Long, val description: String, val userName: String, val identifier: String)

        val tasks = timedTaskRunner.getTasks().map { task ->
            val userName = task.userPrincipal.userEntity.name
            TaskInfo(task.due, task.description, userName, task.identifier)
        }

        connectionService.toUser(userPrincipal.name, ServerActions.SERVER_TASKS, tasks)
    }

    fun removeTaskWithExactIdentifiers(identifiers: Map<String, String>): TaskInfo {
        return timedTaskRunner.removeSpecific(identifiers)
    }

}


private class TimedTask(
    val due: Long,
    val description: String,
    val userPrincipal: UserPrincipal,
    val action: () -> Unit,
    val identifier: String,
)

private class TimedTaskRunner {

    private val timedTasks = LinkedList<TimedTask>()

    private var nextDue: Long? = null

    fun getEvent(): TimedTask? {
        if (nextDue == null || nextDue!! > System.currentTimeMillis()) {
            return null
        }
        val task = timedTasks.removeFirst()
        this.nextDue = timedTasks.firstOrNull()?.due
        return task
    }

    fun queueInMillis(description: String, identifiers: Map<String, String>, due: Long, userPrincipal: UserPrincipal, action: () -> Unit) {
        val identifier = toIdentifierString(identifiers)
        val task = TimedTask(due, description, userPrincipal, action, identifier)
        timedTasks.add(task)
        this.sort()
    }

    fun toIdentifierString(identifiers: Map<String, String>): String {
        return identifiers.entries.sortedBy { it.key }.joinToString(", ") { "${it.key}=${it.value}" }
    }

    fun removeAll(identifiers: Map<String, String>) {
        val identifier = toIdentifierString(identifiers)

        timedTasks.removeIf { task -> task.identifier.contains(identifier) }
        this.sort()
    }

    fun removeSpecific(identifiers: Map<String, String>): TaskInfo {
        val identifier = toIdentifierString(identifiers)
        val toRemove: TimedTask = timedTasks.find { task -> task.identifier == identifier } ?: error("Task not found")

        timedTasks.remove(toRemove)
        this.sort()

        val dueInSeconds = (toRemove.due - System.currentTimeMillis()) / 1000
        val dueAtLocalMillis = toRemove.due

        return TaskInfo(dueInSeconds, dueAtLocalMillis, toRemove.description)
    }

    fun sort() {
        if (timedTasks.isEmpty()) {
            this.nextDue = null
            return
        }
        timedTasks.sortBy { it.due }
        this.nextDue = timedTasks.first().due
    }

    fun getTasks(): List<TimedTask> {
        return timedTasks
    }
}
