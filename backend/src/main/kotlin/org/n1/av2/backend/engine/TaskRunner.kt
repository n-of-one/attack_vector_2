package org.n1.av2.backend.engine

import mu.KLogging
import org.n1.av2.backend.model.db.user.HackerIcon
import org.n1.av2.backend.model.db.user.User
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.util.FatalException
import org.n1.av2.backend.util.ServerFatal
import org.springframework.stereotype.Component
import java.security.Principal
import java.util.concurrent.LinkedBlockingQueue
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

private class Task(val action: () -> Unit, val user: User)

/**
 * All actions are performed on a single thread, this way the execution becomes very predictable,
 * and we don't worry about parallel execution in the rest of our code.
 */
@Component
class TaskRunner(
    val taskEngine: TaskEngine,
    val timedTaskRunner: TimedTaskRunner,
    val currentUserService: CurrentUserService) {

    fun runTask(principal: Principal, action: () -> Unit) {
        if (principal !is UserPrincipal) error("Received call where principal is not UserPrincipal: ${principal}")

        taskEngine.runForUser(principal.user, action)
    }

    /// run is ambiguous with Kotlin's extension function: run. So we implement it ourselves to prevent bugs
    @Deprecated(message=">> Do not use run, Use runTask() instead", level=DeprecationLevel.ERROR, replaceWith = ReplaceWith("taskRunner.runTask(principal) {}"))
    fun run(action: () -> Unit) {
        error(">> Do not use run, this is ambiguous with Kotlin run. Use runTask() instead")
    }

    fun queueInTicks(waitTicks: Int, action: () -> Unit) {
        val due = System.currentTimeMillis() + TICK_MILLIS * waitTicks
        timedTaskRunner.add(currentUserService.userId, due, currentUserService.user, action)
    }

    fun queueInSeconds(omniId: String, seconds: Int, user: User, action: () -> Unit) {
        val due = System.currentTimeMillis() + SECOND_MILLIS * seconds
        timedTaskRunner.add(omniId, due, user, action)
    }

    fun queueInMinutesAndSeconds(minutes: Long, seconds: Long, action: () -> Unit) {
        val due = System.currentTimeMillis() + SECOND_MILLIS * seconds + MINUTE_MILLIS * minutes
        timedTaskRunner.add(currentUserService.userId, due, currentUserService.user, action)
    }
}

@Component
class TaskEngine (val stompService: StompService,
                 val currentUserService: CurrentUserService): Runnable {

    companion object : KLogging()

    private val queue = LinkedBlockingQueue<Task>()
    private var running = true

    @PostConstruct
    fun init() {
        Thread(this).start()
    }

    fun runForUser(user: User, action: () -> Unit) {
        val task = Task(action, user)
        queue.put(task)
    }

    override fun run() {
        while (running) {
            runTask()
        }
    }

    private fun runTask() {
        val task = queue.take()
        if (!running) return // this will happen if fun terminate() unblocked the queue.
        try {
            currentUserService.set(task.user)
            task.action()
        } catch (exception: Exception) {
            if (exception is InterruptedException) {
                throw exception
            }
            if (exception is ValidationException) {
                stompService.toUser(exception.getNoty())
                return
            }
            if (exception is FatalException) {
                val event = ServerFatal(false, exception.message ?: "-")
                stompService.toUser(ReduxActions.SERVER_ERROR, event)
                logger.warn("${task.user.name}: ${exception}")
                return
            }
            if (!currentUserService.isSystemUser) {
                val event = ServerFatal(true, exception.message ?: "-")
                stompService.toUser(ReduxActions.SERVER_ERROR, event)
                logger.info("${task.user.name} - task triggered exception. ", exception)
            } else {
                logger.info("SYSTEM - task triggered exception. ", exception)
            }
        } finally {
            currentUserService.remove()
        }
    }

    @PreDestroy
    fun terminate() {
        this.running = false
        val systemUser = User(id = SYSTEM_USER_ID, name = "System", icon = HackerIcon.BEAR)

        // Add a task to unblock the running thread in the likely case it's blocked waiting on the queue.
        this.queue.add(Task({}, systemUser ))
    }
}
