package org.n1.av2.script

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
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

    @MessageMapping("/gm/scriptAccess/add")
    fun addScriptAccess(command: AddScriptCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptAccessService.addScriptAccess(command.typeId, command.userId)
        }
    }

    @MessageMapping("/gm/scriptAccess/delete")
    fun addScriptAccess(accessId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptAccessService.deleteAccess(accessId)
        }
    }

    class ScriptEditCommand(val id: String, val receiveForFree: Int, val price: BigDecimal?)

    @MessageMapping("/gm/scriptAccess/edit")
    fun editScriptAccess(command: ScriptEditCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptAccessService.editAccess(command.id, command.receiveForFree, command.price)
        }
    }

//    @MessageMapping("/script/list")
//    fun listScripts(userPrincipal: UserPrincipal) {
//        userTaskRunner.runTask(userPrincipal) {
//            scriptService.findForUser(userPrincipal.name)
//        }
//    }
//
//    @MessageMapping("/script/removeObsolete")
//    fun removeObsolete() {
//        userTaskRunner.runTask("system") {
//            scriptService.removeObsolete()
//        }
//    }

}
