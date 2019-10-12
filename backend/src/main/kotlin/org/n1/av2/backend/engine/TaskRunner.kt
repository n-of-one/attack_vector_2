package org.n1.av2.backend.engine

import mu.KLogging
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.util.FatalException
import org.n1.av2.backend.util.ServerFatal
import java.util.concurrent.LinkedBlockingQueue

class TaskRunner(val queue: LinkedBlockingQueue<Task>,
                 val stompService: StompService,
                 val currentUserService: CurrentUserService): Runnable {

    companion object: KLogging()

    var running = true

    override fun run() {
        try {
            while (running) {
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
            currentUserService.set(task.user)
            task.action()
            currentUserService.remove()
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
                logger.warn("${task.user.name}: ${exception}")
                return
            }
            if (!currentUserService.isSystemUser) {
                val event = ServerFatal(true, exception.message ?: "-")
                stompService.toUser(ReduxActions.SERVER_ERROR, event)
                logger.info("${task.user.name} - task triggered exception. ", exception)
            }
            else {
                logger.info("SYSTEM - task triggered exception. ", exception)
            }
        }
    }

    fun terminate() {
        this.running = false
    }

}
