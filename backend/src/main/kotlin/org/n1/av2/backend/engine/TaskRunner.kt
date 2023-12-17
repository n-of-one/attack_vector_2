package org.n1.av2.backend.engine

import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.util.FatalException
import org.n1.av2.backend.util.ServerFatal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

const val TICK_MILLIS = 50
const val SECOND_MILLIS = 1000
const val SECONDS_IN_TICKS = SECOND_MILLIS / TICK_MILLIS

data class TaskIdentifiers(val userId: String?, val siteId: String?, val layerId: String?)

private class Task(val action: () -> Unit, val userPrincipal: UserPrincipal)

/**
 * All actions are performed on a single thread, this way the execution becomes very predictable,
 * and we don't worry about parallel execution in the rest of our code.
 */
@Component
class UserTaskRunner(
    private val taskEngine: TaskEngine,
    private val currentUserService: CurrentUserService
) {

    fun runTask(userPrincipal: UserPrincipal, action: () -> Unit) {
        taskEngine.runForUser(userPrincipal, action)
    }

    fun queueInTicksForSite(siteId: String, waitTicks: Int, action: () -> Unit) {
        val identifiers = TaskIdentifiers(currentUserService.userId, siteId, null)
        queueInTicks(identifiers, waitTicks, action)
    }

    fun queueInTicks(identifiers: TaskIdentifiers, waitTicks: Int, action: () -> Unit) {
        val due = System.currentTimeMillis() + TICK_MILLIS * waitTicks
        val userPrincipal = SecurityContextHolder.getContext().authentication as UserPrincipal
        taskEngine.queueInMillis(identifiers, due, userPrincipal, action)
    }


    /// run is ambiguous with Kotlin's extension function: run. So we implement it ourselves to prevent bugs
    @Deprecated(
        message = ">> Do not use run, Use runTask() instead",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("userTaskRunner.runTask(principal) {}")
    )
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

    fun queueInSeconds(identifiers: TaskIdentifiers, seconds: Long, action: () -> Unit) {
        val due = System.currentTimeMillis() + seconds * 1000
        taskEngine.queueInMillis(identifiers, due, UserPrincipal.system(), action)
    }

    /// run is ambiguous with Kotlin's extension function: run. So we implement it ourselves to prevent bugs
    @Deprecated(
        message = ">> Do not use run, Use runTask() instead",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("taskRunner.runTask(principal) {}")
    )
    fun run(action: () -> Unit) {
        error(">> Do not use run, this is ambiguous with Kotlin run. Use runTask() instead")
    }
}

@Component
class TaskEngine(
    val stompService: StompService,
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

    fun runForUser(userPrincipal: UserPrincipal, action: () -> Unit) {
        val task = Task(action, userPrincipal)
        queue.put(task)
    }

    fun queueInMillis(identifiers: TaskIdentifiers, due: Long, userPrincipal: UserPrincipal, action: () -> Unit) {
        timedTaskRunner.queueInMillis(identifiers, due, userPrincipal, action)
    }

    override fun run() {
        while (running) {
            runTask()
            checkTimedTasks()
        }
    }

    private fun runTask() {
        val task = queue.poll(1, TimeUnit.MILLISECONDS) ?: return
        try {
            currentUserService.set(task.userPrincipal.userEntity)
            SecurityContextHolder.getContext().authentication = task.userPrincipal
            task.action()
        } catch (exception: Exception) {
            if (exception is InterruptedException) {
                throw exception
            }
            if (exception is ValidationException) {
                stompService.replyMessage(exception.getNoty())
                return
            }
            if (exception is FatalException) {
                val event = ServerFatal(false, exception.message ?: exception.javaClass.name)
                stompService.reply(ServerActions.SERVER_ERROR, event)
                logger.warn("${task.userPrincipal.userEntity.name}: ${exception}")
                return
            }
            if (!currentUserService.isSystemUser) {
                val event = ServerFatal(true, exception.message ?: exception.javaClass.name)
                stompService.reply(ServerActions.SERVER_ERROR, event)
                logger.info("User: ${task.userPrincipal.userEntity.name} - task triggered exception. ", exception)
            } else {
                logger.info("SYSTEM - task triggered exception. ", exception)
            }
        } catch (error: Throwable) {
            logger.error("Task triggered error.", error)
            val event = ServerFatal(false, error.message ?: error.javaClass.name)
            stompService.reply(ServerActions.SERVER_ERROR, event)
            return
        } finally {
            currentUserService.remove()
            SecurityContextHolder.clearContext()
        }
    }

    @PreDestroy
    fun terminate() {
        this.running = false
    }

    fun removeForUser(userId: String) {
        this.queue.removeIf { it.userPrincipal.userId == userId }
    }

    fun removeAll(identifiers: TaskIdentifiers) {
        timedTaskRunner.removeAll(identifiers)
        if (identifiers.userId != null) {
            removeForUser(identifiers.userId)
        }
    }

    fun checkTimedTasks() {
        val event = timedTaskRunner.getEvent() ?: return
        runForUser(event.userPrincipal, event.action)
    }
}



private class TimedTask(
    val due: Long,
    val userPrincipal: UserPrincipal,
    val action: () -> Unit,
    val identifiers: TaskIdentifiers,
)

private class TimedTaskRunner {

    private val timedTasks = LinkedList<TimedTask>()

    private var nextDue:Long? = null

    fun getEvent(): TimedTask? {
        if (nextDue == null || nextDue!! > System.currentTimeMillis()) {
            return null
        }
        val task = timedTasks.removeFirst()
        this.nextDue = timedTasks.firstOrNull()?.due
        return task
    }

    fun queueInMillis(identifiers: TaskIdentifiers, due: Long, userPrincipal: UserPrincipal, action: () -> Unit) {
        val task = TimedTask(due, userPrincipal, action, identifiers)
        timedTasks.add(task)
        this.sort()
    }

    fun removeAll(identifiers: TaskIdentifiers) {
        timedTasks.removeIf { task ->
            task.identifiers.userId == identifiers.userId ||
                    task.identifiers.siteId == identifiers.siteId ||
                    task.identifiers.layerId == identifiers.layerId
        }
        this.sort()
    }

    fun sort() {
        if (timedTasks.isEmpty()) {
            this.nextDue = null
            return
        }
        timedTasks.sortBy { it.due }
        this.nextDue = timedTasks.first().due
    }
}
