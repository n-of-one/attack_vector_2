package org.n1.av2.script.type

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.script.effect.ScriptEffectType
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.math.BigDecimal

@Controller
class ScriptTypeWsController(
    private val userTaskRunner: UserTaskRunner,
    private val scriptTypeService: ScriptTypeService,
) {


    @MessageMapping("/gm/scriptType/getAll")
    fun getScriptTypes(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptTypeService.sendScriptTypes()
        }
    }

    @MessageMapping("/gm/scriptType/add")
    fun addScript(name: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptTypeService.add(name)
        }
    }

    class EditCommand(val scriptTypeId: String, val name: String, val ram: Int, val defaultPrice: BigDecimal?)
    @MessageMapping("/gm/scriptType/edit")
    fun editScript(command: EditCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptTypeService.edit(command.scriptTypeId, command.name, command.ram, command.defaultPrice)
        }
    }

    class AddEffectCommand(val scriptTypeId: String, val effectType: ScriptEffectType)
    @MessageMapping("/gm/scriptType/addEffect")
    fun addEffect(command: AddEffectCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptTypeService.addEffect(command.scriptTypeId, command.effectType)
        }
    }
    class DeleteEffectCommand(val scriptTypeId: String, val effectNumber: Int)
    @MessageMapping("/gm/scriptType/deleteEffect")
    fun deleteEffect(command: DeleteEffectCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptTypeService.deleteEffect(command.scriptTypeId, command.effectNumber)
        }
    }
    class EditEffectCommand(val scriptTypeId: String, val effectNumber: Int, val value: String)
    @MessageMapping("/gm/scriptType/editEffect")
    fun editEffect(command: EditEffectCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            scriptTypeService.editEffect(command.scriptTypeId, command.effectNumber, command.value)
        }
    }
}
