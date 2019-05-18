package org.n1.mainframe.backend.engine

import mu.KLogging
import org.n1.mainframe.backend.model.ui.ValidationException
import org.n1.mainframe.backend.service.PrincipalService
import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.n1.mainframe.backend.util.FatalException
import org.n1.mainframe.backend.util.ServerFatal
import java.util.concurrent.LinkedBlockingQueue

class TaskRunner(val queue: LinkedBlockingQueue<Task>,
                 val stompService: StompService,
                 val principalService: PrincipalService): Runnable {

    companion object: KLogging()

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
            principalService.set(task.principal)
            task.action()
            principalService.remove()
        }
        catch (exception: Exception) {
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
                logger.warn("${task.principal.name}: ${exception}")
                return
            }
            val event = ServerFatal(true, exception.message ?: "-")
            stompService.toUser(ReduxActions.SERVER_ERROR, event)
            logger.info("${task.principal.name} - task triggered exception. ", exception)
        }
    }

}
