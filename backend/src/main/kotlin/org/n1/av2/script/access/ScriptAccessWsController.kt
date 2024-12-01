package org.n1.av2.script.access

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.n1.av2.script.ScriptService
import org.n1.av2.script.access.ScriptAccessService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.math.BigDecimal

@Controller
class ScriptAccessWsController(
    private val userTaskRunner: UserTaskRunner,
    private val scriptService: ScriptService,
    private val scriptAccessService: ScriptAccessService,
    private val connectionService: ConnectionService,
    private val userAndHackerService: UserAndHackerService,
) {

    @MessageMapping("/gm/scriptAccess/get")
    fun getScriptAccess(userId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptAccessService.sendScriptAccess(userId)
            userAndHackerService.sendDetailsOfSpecificUser(userId) // trigger showing page of this user

        }
    }

    class AddScriptCommand(val typeId: String, val userId: String)
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
