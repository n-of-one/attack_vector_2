package org.n1.av2.backend.engine

import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Component
import java.security.Principal
import java.util.concurrent.LinkedBlockingQueue
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * This is the class that executes all incoming requests using a single thread
 * in order to get Serializable isolation.
 */
@Component
class SerializingExecutor(
        stompService: StompService,
        currentUserService: CurrentUserService,
        timedEventQueue: TimedEventQueue,
        gameEventService: GameEventService)  {

    private val queue = LinkedBlockingQueue<Task>()
    val runner = TaskRunner(queue, stompService, currentUserService)
    val eventRunner = TimedEventRunner(timedEventQueue, queue, gameEventService)
    val executorThread = Thread(runner)
    val timedEventThread = Thread(eventRunner)

    @PostConstruct
    fun init() {
        executorThread.start()
        timedEventThread.start()
    }

    @PreDestroy
    fun destroy() {
        // unlike shutdown() shutdownNow() sends interruption to running tasks
        runner.terminate()
    }

    fun run(principal: Principal, action: () -> Unit) {
        val task = Task(action, principal)
        queue.put(task)
    }


}