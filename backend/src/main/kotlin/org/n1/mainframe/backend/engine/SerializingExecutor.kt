package org.n1.mainframe.backend.engine

import org.n1.mainframe.backend.service.PrincipalService
import org.n1.mainframe.backend.service.StompService
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
class SerializingExecutor(stompService: StompService, principalService: PrincipalService)  {

    private val queue = LinkedBlockingQueue<Task>()
    val executorService = Executors.newSingleThreadExecutor()!!
    val runner = TaskRunner(queue, stompService, principalService)

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

}