package org.n1.av2.admin

import org.n1.av2.platform.engine.TaskEngine
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class AdminWsController(
    val userTaskRunner: UserTaskRunner,
    val taskEngine: TaskEngine
) {


    @MessageMapping("/admin/monitorTasks")
    fun terminalMain(userPrincipal: UserPrincipal) {
        taskEngine.sendTasks(userPrincipal)
    }

}
