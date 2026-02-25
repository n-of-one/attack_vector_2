package org.n1.av2.hacker.skill

import org.n1.av2.script.ram.RamService
import org.n1.av2.site.entity.enums.LayerType
import org.n1.av2.site.entity.enums.LayerType.TANGLE_ICE
import org.n1.av2.site.entity.enums.LayerType.WORD_SEARCH_ICE
import org.springframework.context.ApplicationContext
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

typealias SkillId = String

@Document
data class Skill(
    @Id val id: SkillId,
    @Indexed val userId: String,
    val type: SkillType,
    val value: String?,
    val usedOnSiteIds: List<String>
) {
    constructor(id: SkillId, userId: String, type: SkillType, value: String?) : this(id, userId, type, value, emptyList())
}

@Repository
interface SkillRepo : MongoRepository<Skill, SkillId> {
    fun findByUserId(userId: String): List<Skill>
}

fun List<Skill>.containsType(type: SkillType) = this.any { it.type == type }


enum class SkillType(
    val defaultValue: String? = null,
    val validate: ((String) -> String?)? = null,
    val toFunctionalValue: (String) -> String = noOpNormalization,
    val toDisplayValue: (String) -> String = displayAsIs,
    val processUpdate: (userId: String, value: String?, context: ApplicationContext) -> Unit = noUpdateAction,
    val processSkillRemoval: (userId: String, context: ApplicationContext) -> Unit = noRemovalAction,
) {
    ADJUSTED_SPEED("4", ::validateHackerSpeed),
    BYPASS(),
    CREATE_SITE(),
    JUMP_TO_HACKER(),
    SCAN(),
    SEARCH_SITE(),
    SCRIPT_CREDITS("0", ::validatePositiveNumber),
    SCRIPT_RAM("3", ::validatePositiveNumber, noOpNormalization, displayAsIs, ::ramSkillUpdate, ::ramSkillRemoval),
    STEALTH("30", stealthValidation, stealthToFunctional, stealthToDisplay),
    UNDO_TRIPWIRE(),
    WEAKEN("${WORD_SEARCH_ICE.name.substringBefore("_ICE").lowercase()}, ${TANGLE_ICE.name.substringBefore("_ICE").lowercase()}", ::validateIceTypes),
}

val noOpNormalization = { toNormalize: String -> toNormalize }
val displayAsIs = { toDisplay: String -> toDisplay }
val noUpdateAction = { _: String, _: String?, _: ApplicationContext -> }
val noRemovalAction = { _: String, _: ApplicationContext -> }



fun validatePositiveNumber(input: String): String? {
    val value = input.toIntOrNull() ?: return "must be a whole number"
    if (value < 0) return "must be a positive number"
    return null
}

fun validateHackerSpeed(input: String): String? {
    val value = input.toIntOrNull() ?: return "must be a whole number"
    if (value < 1) return "Terminal speed must be at least 1"
    return null
}

fun ramSkillUpdate(userId: String, value: String?, context: ApplicationContext) {
    context.getBean(RamService::class.java).alterRamSize(userId, value!!.toInt())
}

fun ramSkillRemoval(userId: String, context: ApplicationContext) {
    context.getBean(RamService::class.java).alterRamSize(userId, 0)
}

val stealthValidation = { toValidate: String ->
    val value = stealthToFunctional(toValidate)
    val percentage = value.toIntOrNull()
    when (percentage) {
        null -> "Stealth skill requires a percentage value"
        0 -> "Stealth skill of 0% has no effect, this means the time-out is increased by 0%, meaning it stays the same."
        !in -100..1000 -> "Stealth value must be between -100 and 1000 (%)."
        else -> null
    }
}
val stealthToDisplay = { toNormalize: String ->
    val percentage = toNormalize.toInt()
    val prefix = if (percentage >= 0) "+" else ""

    "${prefix}${percentage}%"
}

val stealthToFunctional = { input: String ->
    input.removeSuffix("%").removePrefix("+")
}

fun validateIceTypes(input: String): String? {
    val parts = input.split(",").map { it.trim() }
    if (parts.first().trim().isEmpty()) return "Must provide the type of ICE this skill works on. $iceOptionsErrorMessage"

    parts.forEach { part ->
        val toMatch = if (!part.endsWith("_ICE")) "${part}_ICE" else part
        val layerType = LayerType.valueOfOrNull(toMatch.uppercase())

        if (layerType?.ice != true) return "Invalid value: ${part}. $iceOptionsErrorMessage"
    }
    return null
}

private val iceOptionsErrorMessage = "Options are: ${iceOptions()}. Separate options with a comma (,)"

private fun iceOptions() = LayerType.entries
    .filter { it.ice }
    .map { it.toString().substringBefore("_ICE").lowercase() }
    .sorted()
    .joinToString(", ")

