package org.n1.av2.script.type

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.util.createId
import org.n1.av2.script.effect.ScriptEffect
import org.n1.av2.script.effect.ScriptEffectType
import org.n1.av2.script.effect.ScriptEffectWithValue
import org.springframework.stereotype.Service
import java.math.BigDecimal
import kotlin.jvm.optionals.getOrElse

@Service
class ScriptTypeService(
    val scriptTypeRepository: ScriptTypeRepository,
    val connectionService: ConnectionService,
) {

    fun getById(id: String): ScriptType {
        return scriptTypeRepository.findById(id).getOrElse { error("Script type not found with id: $id") }
    }

    class ScriptTypeEffectUi(
        val id: String,
        val value: String?,
        val name: String,
        val playerDescription: String,
        val gmDescription: String,
    )

    class ScriptTypeUI(
        val id: String,
        val name: String,
        val ram: Int,
        val defaultPrice: BigDecimal?,
        val effects: List<ScriptTypeEffectUi>
    )

    fun sendScriptTypes() {
        val uiScriptTypes = scriptTypeRepository.findAll().map { scriptType: ScriptType ->
            ScriptTypeUI(
                id = scriptType.id,
                name = scriptType.name,
                ram = scriptType.ram,
                defaultPrice = scriptType.defaultPrice,
                effects = scriptType.effects.mapIndexed { index: Int, effect: ScriptEffect ->
                    ScriptTypeEffectUi(
                        id = (index + 1).toString(),
                        value = when (effect) {
                            is ScriptEffectWithValue -> effect.value
                            else -> null
                        },
                        name = effect.name,
                        playerDescription = effect.playerDescription,
                        gmDescription = effect.gmDescription,
                    )
                }
            )
        }
        connectionService.reply(ServerActions.SERVER_SCRIPT_TYPES, uiScriptTypes)

    }

    fun add(name: String) {
        val id = createId("type", scriptTypeRepository::findById)
        val scriptType = ScriptType(
            id = id,
            name = name,
            ram = 1,
            defaultPrice = null,
            effects = emptyList()
        )
        scriptTypeRepository.save(scriptType)
        sendScriptTypes()
        connectionService.reply(ServerActions.SERVER_RECEIVE_EDIT_USER, id)
    }

    fun edit(scriptTypeId: String, name: String, ram: Int, defaultPrice: BigDecimal?) {
        val scriptType = getById(scriptTypeId)

        val editedScriptType = scriptType.copy(
            name = name,
            ram = when {
                ram < 1 -> 0
                else -> ram
            },
            defaultPrice = when {
                defaultPrice == null -> null
                defaultPrice <= BigDecimal.ZERO -> null
                else -> defaultPrice
            }
        )

        scriptTypeRepository.save(editedScriptType)
        sendScriptTypes()
    }

    fun addEffect(scriptTypeId: String, type: ScriptEffectType) {
        val scriptType = getById(scriptTypeId)
        val effect = type.createEffect()
        val editedScriptType = scriptType.copy(
            effects = scriptType.effects + effect
        )
        scriptTypeRepository.save(editedScriptType)
        sendScriptTypes()
    }

    fun deleteEffect(scriptTypeId: String, effectNumber: Int) {
        val scriptType = getById(scriptTypeId)

        val effectToDelete = scriptType.effects.getOrNull(effectNumber.toInt() - 1) ?: error("Effect with number ${effectNumber} not found")

        val editedScriptType = scriptType.copy(
            effects = scriptType.effects
                .filterNot { it == effectToDelete }
        )
        scriptTypeRepository.save(editedScriptType)
        sendScriptTypes()
    }

    fun editEffect(scriptTypeId: String, effectNumber: Int, value: String) {
        val scriptType = getById(scriptTypeId)

        val effectToEdit = scriptType.effects.getOrNull(effectNumber.toInt() - 1) ?: error("Effect with number ${effectNumber} not found")
        if (effectToEdit !is ScriptEffectWithValue) {
            error("Effect with number ${effectNumber} is not editable")
        }

        val validationErrorMessage = effectToEdit.validate(value)
        if (validationErrorMessage != null) {
            sendScriptTypes()
            error(validationErrorMessage)
        }

        val editedEffect = effectToEdit.copyWithNewValue(value)

        val editedScriptType = scriptType.copy(
            effects = scriptType.effects
                .map { if (it == effectToEdit) editedEffect else it }
        )
        scriptTypeRepository.save(editedScriptType)
        sendScriptTypes()
    }

    fun findAll(): List<ScriptType> {
        return scriptTypeRepository.findAll().toList()
    }
}
