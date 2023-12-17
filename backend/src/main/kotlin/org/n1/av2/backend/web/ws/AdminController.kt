package org.n1.av2.backend.web.ws

import org.n1.av2.backend.engine.TaskEngine
import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.run.TerminalService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class AdminController(
    val userTaskRunner: UserTaskRunner,
    val taskEngine: TaskEngine
) {


    @MessageMapping("/admin/monitorTasks")
    fun terminalMain(userPrincipal: UserPrincipal) {
        taskEngine.sendTasks(userPrincipal)
    }

}