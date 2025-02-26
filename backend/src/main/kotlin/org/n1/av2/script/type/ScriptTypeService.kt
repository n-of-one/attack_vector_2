package org.n1.av2.script.type

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.util.createId
import org.n1.av2.script.Script
import org.n1.av2.script.ScriptService
import org.n1.av2.script.access.ScriptAccessService
import org.n1.av2.script.effect.ScriptEffectLookup
import org.springframework.stereotype.Service
import java.math.BigDecimal
import javax.annotation.PostConstruct
import kotlin.jvm.optionals.getOrElse

@Service
class ScriptTypeServiceInit(
    private val ScriptTypeService: ScriptTypeService,
    private val ScriptService: ScriptService,
    private val scriptAccessService: ScriptAccessService) {

    @PostConstruct
    fun postConstruct() {
        ScriptTypeService.scriptService = ScriptService
        ScriptTypeService.scriptAccessService = scriptAccessService
    }
}


@Service
class ScriptTypeService(
    private val scriptTypeRepository: ScriptTypeRepository,
    private val connectionService: ConnectionService,
    private val effectService: ScriptEffectLookup,
)  {

    lateinit var scriptService: ScriptService
    lateinit var scriptAccessService: ScriptAccessService

    fun getById(id: ScriptTypeId): ScriptType {
        return scriptTypeRepository.findById(id).getOrElse { error("Script type not found with id: $id") }
    }

    class ScriptTypeEffectUi(
        val value: String?,
        val name: String,
        val playerDescription: String,
        val gmDescription: String,
        val hidden: Boolean,
        val type: ScriptEffectType,
    )

    class ScriptTypeUI(
        val id: ScriptTypeId,
        val name: String,
        val size: Int,
        val defaultPrice: BigDecimal?,
        val effects: List<ScriptTypeEffectUi>
    )

    fun sendScriptTypes() {
        val uiScriptTypes = scriptTypeRepository.findAll().map { scriptType: ScriptType ->

            val hideIndex = scriptType.effects.indexOfFirst { effect -> effect.type == ScriptEffectType.HIDDEN_EFFECTS }

            ScriptTypeUI(
                id = scriptType.id,
                name = scriptType.name,
                size = scriptType.size,
                defaultPrice = scriptType.defaultPrice,
                effects = scriptType.effects.mapIndexed { index: Int, effect: ScriptEffect ->
                    val effectService = effectService.getForType(effect.type)
                    ScriptTypeEffectUi(
                        value = effect.value,
                        name = effectService.name,
                        playerDescription = effectService.playerDescription(effect),
                        gmDescription = effectService.gmDescription,
                        hidden = (hideIndex > -1 && index > hideIndex),
                        type = effect.type

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
            size = 1,
            defaultPrice = null,
            effects = emptyList()
        )
        scriptTypeRepository.save(scriptType)
        sendScriptTypes()
        connectionService.reply(ServerActions.SERVER_EDIT_SCRIPT_TYPE, id)

    }

    fun edit(scriptTypeId: ScriptTypeId, name: String, size: Int, defaultPrice: BigDecimal?) {
        val scriptType = getById(scriptTypeId)

        val editedScriptType = scriptType.copy(
            name = name,
            size = when {
                size < 1 -> 0
                else -> size
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

    fun addEffect(scriptTypeId: ScriptTypeId, type: ScriptEffectType) {
        val scriptType = getById(scriptTypeId)
        val effectService = effectService.getForType(type)
        val effect = ScriptEffect(
            type = type,
            value = effectService.defaultValue
        )
        val editedScriptType = scriptType.copy(
            effects = scriptType.effects + effect
        )
        scriptTypeRepository.save(editedScriptType)
        sendScriptTypes()
    }

    fun deleteEffect(scriptTypeId: ScriptTypeId, effectNumber: Int) {
        val scriptType = getById(scriptTypeId)

        val effectToDelete = scriptType.effects.getOrNull(effectNumber.toInt() - 1) ?: error("Effect with number ${effectNumber} not found")

        val editedScriptType = scriptType.copy(
            effects = scriptType.effects
                .filterNot { it == effectToDelete }
        )
        scriptTypeRepository.save(editedScriptType)
        sendScriptTypes()
    }

    fun editEffect(scriptTypeId: ScriptTypeId, effectNumber: Int, value: String) {
        val scriptType = getById(scriptTypeId)

        val effectToEdit = scriptType.effects.getOrNull(effectNumber.toInt() - 1) ?: error("Effect with number ${effectNumber} not found")


        val effectService = effectService.getForType(effectToEdit.type)

        val effectWithNewValue = effectToEdit.copy(value = value)

        val validationErrorMessage = effectService.validate(effectWithNewValue)
        if (validationErrorMessage != null) {
            sendScriptTypes()
            error(validationErrorMessage)
        }

        val editedScriptType = scriptType.copy(
            effects = scriptType.effects
                .map { if (it == effectToEdit) effectWithNewValue else it }
        )
        scriptTypeRepository.save(editedScriptType)
        sendScriptTypes()
    }

    fun findAll(): List<ScriptType> {
        return scriptTypeRepository.findAll().toList()
    }

    fun delete(scriptTypeId: ScriptTypeId) {
        val scriptType = getById(scriptTypeId)
        val existingScripts: List<Script> = scriptService.findByTypeId(scriptTypeId )
        val usableExistingScripts = existingScripts.filter { scriptService.usable(it)}

        if (usableExistingScripts.isNotEmpty()) {
            error("Cannot delete ${scriptType.name} because there are still ${usableExistingScripts.size} scripts of this type.")
        }

        val accesses = scriptAccessService.findByTypeId(scriptTypeId)
        if (accesses.isNotEmpty()) {
            error("Cannot delete ${scriptType.name} because there are still ${accesses.size} access entries for this type.")
        }

        val unusableScripts = existingScripts - usableExistingScripts
        unusableScripts.forEach { script: Script -> scriptService.deleteScript(script.id) }

        scriptTypeRepository.deleteById(scriptTypeId)
        sendScriptTypes()
    }

}
