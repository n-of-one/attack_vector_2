package org.n1.av2.script.access

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.math.BigDecimal

@Controller
class ScriptAccessWsController(
    private val userTaskRunner: UserTaskRunner,
    private val scriptAccessService: ScriptAccessService,
    private val userAndHackerService: UserAndHackerService,
) {

    @MessageMapping("/gm/scriptAccess/get")
    fun getScriptAccessForGm(userId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptAccess/get", userPrincipal) {
            scriptAccessService.sendScriptAccess(userId)
            userAndHackerService.sendDetailsOfSpecificUser(userId) // trigger showing page of this user
        }
    }

    class AddScriptAccessCommand(val typeId: String, val userId: String)

    @MessageMapping("/gm/scriptAccess/add")
    fun addScriptAccess(command: AddScriptAccessCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptAccess/add", userPrincipal) {
            scriptAccessService.addScriptAccess(command.typeId, command.userId)
        }
    }

    @MessageMapping("/gm/scriptAccess/delete")
    fun addScriptAccess(accessId: ScriptAccessId, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptAccess/delete", userPrincipal) {
            scriptAccessService.deleteAccess(accessId)
        }
    }

    class EditScriptAccessCommand(val id: ScriptAccessId, val receiveForFree: Int, val price: Int?)

    @MessageMapping("/gm/scriptAccess/edit")
    fun editScriptAccess(command: EditScriptAccessCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptAccess/edit", userPrincipal) {
            scriptAccessService.editAccess(command.id, command.receiveForFree, command.price)
        }
    }


}
