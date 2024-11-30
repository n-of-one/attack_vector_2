package org.n1.av2.admin

import org.n1.av2.platform.engine.TaskEngine
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class AdminWsController(
    private val userTaskRunner: UserTaskRunner,
    private val taskEngine: TaskEngine,
    private val userAndHackerService: UserAndHackerService,
) {

    @MessageMapping("/admin/logon")
    fun logon(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {

            // This will trigger the rendering of the page, send last so that no page updates happen after.
            userAndHackerService.sendDetailsOfCurrentUser()
        }
    }


    @MessageMapping("/admin/monitorTasks")
    fun terminalMain(userPrincipal: UserPrincipal) {
        taskEngine.sendTasks(userPrincipal)
    }

}
