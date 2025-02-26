package org.n1.av2.script

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.script.type.ScriptTypeId
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class ScriptWsController(
    private val userTaskRunner: UserTaskRunner,
    private val scriptService: ScriptService,
) {

    class AddScriptCommand(val typeId: ScriptTypeId, val userId: String)

    @MessageMapping("/gm/script/getStatistics")
    fun addScript(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptService.getStatistics()
        }
    }

    @MessageMapping("/gm/script/getForUser")
    fun addScript(userId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptService.sendScriptStatusForUser(userId)
        }
    }

    @MessageMapping("/gm/script/add")
    fun addScript(command: AddScriptCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptService.addScriptAndInformUser(command.typeId, command.userId)
        }
    }

    @MessageMapping("/gm/script/load")
    fun gmLoad(scriptId: ScriptId, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptService.loadScript(scriptId, true)
        }
    }

    @MessageMapping("/hacker/script/cleanup")
    fun cleanup(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptService.cleanup()
        }
    }

    @MessageMapping("/hacker/script/freeReceive")
    fun freeReceive(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptService.addFreeReceiveScriptsForCurrentUser()
        }
    }

    @MessageMapping("/script/delete")
    fun delete(scriptId: ScriptId, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptService.deleteScript(scriptId)
        }
    }

    @MessageMapping("/script/load")
    fun load(scriptId: ScriptId, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptService.loadScript(scriptId)
        }
    }

    @MessageMapping("/script/unload")
    fun unload(scriptId: ScriptId, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptService.unloadScript(scriptId)
        }
    }

}
