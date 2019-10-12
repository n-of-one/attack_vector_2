package org.n1.av2.backend.engine

import org.n1.av2.backend.model.db.user.User
import org.n1.av2.backend.model.iam.UserPrincipal
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

    private final val runner = TaskRunner(queue, stompService, currentUserService)
    val executorThread = Thread(runner)

    private final val eventRunner = TimedEventRunner(timedEventQueue, queue, gameEventService)
    val timedEventThread = Thread(eventRunner)

    @PostConstruct
    fun init() {
        executorThread.start()
        timedEventThread.start()
    }

    @PreDestroy
    fun destroy() {
        runner.terminate()
        eventRunner.terminate()
    }

    fun run(principal: Principal, action: () -> Unit) {

        if (principal is UserPrincipal) {
            run(principal.user, action)
        } else {
            throw IllegalArgumentException("Principal is not a UserPrincipal, but a ${principal.javaClass} and has value: ${principal}")
        }
    }

    fun run(user: User, action: () -> Unit) {
        val task = Task(action, user)
        queue.put(task)
    }


}