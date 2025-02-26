package org.n1.av2.hacker.hacker

import org.n1.av2.script.ram.RamService
import org.springframework.context.ApplicationContext

data class HackerSkill(
    val type: HackerSkillType,
    val value: String?
) {
    constructor(type: HackerSkillType) : this(type, type.defaultValue)
}


enum class HackerSkillType(
    val defaultValue: String? = null,
    val validate: ((String) -> String?)? = null,
    val toFunctionalValue: (String) -> String = noOpNormalization,
    val toDisplayValue: (String) -> String = displayAsIs,
    val processUpdate: (userId: String, value: String, context: ApplicationContext) -> Unit = noUpdate
) {
    CREATE_SITE(),
    SCAN(),
    SEARCH_SITE(),
    SCRIPT_RAM("3", ::validatePositiveNumber, noOpNormalization, displayAsIs, ::ramSkillUpdate),
//    STEALTH("30", stealthValidation, stealthToFunctional, stealthToDisplay, ) // Await confirmation from the organisers who use AV that this is a skill they want
}

val noOpNormalization = { toNormalize: String -> toNormalize }
val displayAsIs = { toDisplay: String -> toDisplay }
val noUpdate = { _: String, _: String, _: ApplicationContext -> }

fun validatePositiveNumber(input: String): String? {
    val value = input.toIntOrNull() ?: return "must be a whole number"
    if (value < 0) return "must be a positive number"
    return null
}

fun ramSkillUpdate(userId: String, value: String, context: ApplicationContext) {
    context.getBean(RamService::class.java).alterRamSize(userId, value.toInt())
}

//
//val stealthValidation = { toValidate: String ->
//    val value = stealthToFunctional(toValidate)
//    val percentage = value.toIntOrNull()
//    when (percentage) {
//        null -> "Stealth skill requires a percentage value"
//        0 -> "Stealth skill of 0% has no effect, this means the time-out is increased by 0%, meaning it stays the same."
//        !in -100..1000 -> "Stealth value must be between -100 and 1000 (%)."
//        else -> null
//    }
//}
//val stealthToDisplay = { toNormalize: String ->
//    val percentage = toNormalize.toInt()
//    val prefix = if (percentage >= 0) "+" else ""
//
//    "${prefix}${percentage}%"
//}
//
//val stealthToFunctional = { input: String ->
//    input.removeSuffix("%").removePrefix("+")
//}
