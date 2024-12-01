package org.n1.av2.script

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.script.access.ScriptAccessService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.math.BigDecimal

@Controller
class ScriptWsController(
    private val userTaskRunner: UserTaskRunner,
    private val scriptService: ScriptService,
    private val scriptAccessService: ScriptAccessService,
) {

    class AddScriptCommand(val typeId: String, val userId: String)
    @MessageMapping("/gm/script/add")
    fun addScript(command: AddScriptCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptService.addScript(command.typeId, command.userId)
        }
    }

    @MessageMapping("/hacker/script/refresh")
    fun refreshScripts(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptService.refreshScriptsForCurrentUser()
        }
    }

    @MessageMapping("/hacker/script/delete")
    fun deleteScript(scriptCode: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptService.deleteScript(scriptCode)
        }
    }


}
