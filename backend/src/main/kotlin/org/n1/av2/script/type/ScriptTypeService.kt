package org.n1.av2.script.type

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.util.createId
import org.n1.av2.script.Script
import org.n1.av2.script.ScriptService
import org.n1.av2.script.access.ScriptAccessService
import org.n1.av2.script.effect.ScriptEffectType
import org.n1.av2.script.effect.ScriptEffectTypeLookup
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
    private val effectService: ScriptEffectTypeLookup,
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

    @Suppress("unused")
    class ScriptTypeUI(
        val id: ScriptTypeId,
        val name: String,
        val gmNote: String,
        val category: String,
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
                category =  scriptType.category,
                size = scriptType.size,
                gmNote = scriptType.gmNote,
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
        scriptTypeRepository.findByNameIgnoreCase(name)?.let {
            error("A script type with name $name already exists")
        }

        val id = createId("type", scriptTypeRepository::findById)
        val scriptType = ScriptType(
            id = id,
            name = name,
            category = "",
            gmNote = "",
            size = 1,
            defaultPrice = null,
            effects = emptyList()
        )
        scriptTypeRepository.save(scriptType)
        sendScriptTypes()
        connectionService.reply(ServerActions.SERVER_EDIT_SCRIPT_TYPE, id)

        checkScriptNameLength(name)
    }

    fun edit(scriptTypeId: ScriptTypeId, name: String, category: String, gmNote: String, size: Int, defaultPrice: BigDecimal?) {
        checkScriptNameLength(name)

        val scriptType = getById(scriptTypeId)

        val editedScriptType = scriptType.copy(
            name = name,
            category = category,
            size = when {
                size < 1 -> 0
                else -> size
            },
            defaultPrice = when {
                defaultPrice == null -> null
                defaultPrice <= BigDecimal.ZERO -> null
                else -> defaultPrice
            },
            gmNote = gmNote,
        )

        scriptTypeRepository.save(editedScriptType)
        sendScriptTypes()
    }

    private fun checkScriptNameLength(name: String) {
        if (name.isEmpty() || name.length > 15) {
            sendScriptTypes()
            error("Script name is invalid. Min 1 character, max is 15 characters. Otherwise it can't be properly shown to the hackers.")
        }
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
