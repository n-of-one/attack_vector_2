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
        userTaskRunner.runTask(userPrincipal) {
            scriptAccessService.sendScriptAccess(userId)
            userAndHackerService.sendDetailsOfSpecificUser(userId) // trigger showing page of this user
        }
    }

    @MessageMapping("/hacker/scriptAccess/get")
    fun getScriptAccessForHacker(userId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptAccessService.sendScriptAccess(userId)
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


}
