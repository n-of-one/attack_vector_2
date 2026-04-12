package org.n1.av2.script.type

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.script.effect.ScriptEffectType
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class ScriptTypeWsController(
    private val userTaskRunner: UserTaskRunner,
    private val scriptTypeService: ScriptTypeService,
) {

    @MessageMapping("/gm/scriptType/getAll")
    fun getScriptTypes(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptType/getAll", userPrincipal) {
            scriptTypeService.sendScriptTypes()
        }
    }

    @MessageMapping("/gm/scriptType/add")
    fun addScriptType(name: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptType/add", userPrincipal) {
            scriptTypeService.add(name)
        }
    }

    data class DeleteScriptTypeCommand(val scriptTypeId: ScriptTypeId, val force: Boolean)
    @MessageMapping("/gm/scriptType/delete")
    fun deleteScriptType(command: DeleteScriptTypeCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptType/delete", userPrincipal) {
            scriptTypeService.delete(command.scriptTypeId, command.force)
        }
    }

    class EditCommand(val scriptTypeId: ScriptTypeId, val name: String, val category: String, val gmNote: String, val size: Int, val defaultPrice: Int?)
    @MessageMapping("/gm/scriptType/edit")
    fun editScriptType(command: EditCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptType/edit", userPrincipal) {
            scriptTypeService.edit(command.scriptTypeId, command.name, command.category, command.gmNote, command.size, command.defaultPrice)
        }
    }

    class AddEffectCommand(val scriptTypeId: ScriptTypeId, val effectType: ScriptEffectType)
    @MessageMapping("/gm/scriptType/addEffect")
    fun addEffect(command: AddEffectCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptType/addEffect", userPrincipal) {
            scriptTypeService.addEffect(command.scriptTypeId, command.effectType)
        }
    }

    class DeleteEffectCommand(val scriptTypeId: ScriptTypeId, val effectNumber: Int)
    @MessageMapping("/gm/scriptType/deleteEffect")
    fun deleteEffect(command: DeleteEffectCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptType/deleteEffect", userPrincipal) {
            scriptTypeService.deleteEffect(command.scriptTypeId, command.effectNumber)
        }
    }

    class EditEffectCommand(val scriptTypeId: ScriptTypeId, val effectNumber: Int, val value: String)
    @MessageMapping("/gm/scriptType/editEffect")
    fun editEffect(command: EditEffectCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptType/editEffect", userPrincipal) {
            scriptTypeService.editEffect(command.scriptTypeId, command.effectNumber, command.value)
        }
    }
}
