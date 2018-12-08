package org.n1.mainframe.backend.engine

import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * This is the class that executes all incoming requests using a single thread
 * in order to get Serializable isolation.
 */
@Component
class SerializingExecutor  {

    private val queue = LinkedBlockingQueue<() -> Unit>()
    val executorService = Executors.newSingleThreadExecutor()!!
    val runner = Runner(queue)

    @PostConstruct
    fun init() {
        executorService.execute(runner)
    }

    @PreDestroy
    fun destroy() {
        // unlike shutdown() shutdownNow() sends interruption to running tasks
        executorService.shutdownNow()
    }

    fun run(task: () -> Unit) {
        queue.put(task)
    }

    class Runner(val queue: LinkedBlockingQueue<() -> Unit>): Runnable {
        override fun run() {
            try {
                while (true) {
                    val task = queue.take()
                    task()
                }
            } catch (e: InterruptedException) {
                // we were interrupted by shutdownNow(), restore interrupted status and exit
                Thread.currentThread().interrupt()
            }

        }
    }

}