package org.n1.mainframe.backend.engine

import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.ValidationException
import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.n1.mainframe.backend.util.FatalException
import org.n1.mainframe.backend.web.ws.EditorController
import org.springframework.stereotype.Component
import java.security.Principal
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * This is the class that executes all incoming requests using a single thread
 * in order to get Serializable isolation.
 */
@Component
class SerializingExecutor(stompService: StompService)  {

    private val queue = LinkedBlockingQueue<Task>()
    val executorService = Executors.newSingleThreadExecutor()!!
    val runner = Runner(queue, stompService)

    @PostConstruct
    fun init() {
        executorService.execute(runner)
    }

    @PreDestroy
    fun destroy() {
        // unlike shutdown() shutdownNow() sends interruption to running tasks
        executorService.shutdownNow()
    }

    fun run(principal: Principal, action: () -> Unit) {
        val task = Task(action, principal)
        queue.put(task)
    }

    class Runner(val queue: LinkedBlockingQueue<Task>,
                 val stompService: StompService): Runnable {
        override fun run() {
            try {
                while (true) {
                    runTask()
                }
            } catch (e: InterruptedException) {
                // we were interrupted by shutdownNow(), restore interrupted status and exit
                Thread.currentThread().interrupt()
            }

        }

        private fun runTask() {
            val task = queue.take()
            try {
                task.action()
            }
            catch (exception: Exception) {
                if (exception is InterruptedException) {
                    throw exception
                }
                if (exception is ValidationException) {
                    stompService.toUser(task.principal, exception.getNoty())
                    return
                }
                if (exception is FatalException) {
                    stompService.toUser(task.principal, ReduxActions.SERVER_FATAL, exception.message!!)
                    return
                }
                EditorController.logger.error(exception.message, exception)
                val noty = NotyMessage("Problem", "Error:", exception.message ?: "")
                stompService.errorToUser(task.principal, noty)
            }
        }

    }

}