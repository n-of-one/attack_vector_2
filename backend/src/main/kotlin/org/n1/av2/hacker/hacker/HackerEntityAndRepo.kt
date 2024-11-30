package org.n1.av2.hacker.hacker

import org.n1.av2.platform.iam.user.HackerIcon
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository

@Document
data class Hacker(
    @Id val id: String,
    val hackerUserId: String,
    val icon: HackerIcon,
    val characterName: String,
    val skills: List<HackerSkill>
) {
    fun hasSKill(requestType: HackerSkillType): Boolean {
        return skills.any { it.type == requestType }
    }

    fun skillAsIntOrNull(requestType: HackerSkillType): Int? {
        val skill = skills.find { it.type == requestType }
        val value = skill?.value?.toIntOrNull()
        return value
    }

}

data class HackerSkill(
    val type: HackerSkillType,
    val value: String?
) {
    constructor(type: HackerSkillType) : this(type, type.defaultValue)
}

val noOpNormalization = { toNormalize: String -> toNormalize }
val displayAsIs = { toDisplay: String -> toDisplay }
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

enum class HackerSkillType(
    val defaultValue: String? = null,
    val validate: ((String) -> String?)? = null,
    val toFunctionalValue: (String) -> String = noOpNormalization,
    val toDisplayValue: (String) -> String = displayAsIs
) {
    CREATE_SITE(),
    SCAN(),
    SEARCH_SITE(),
//    STEALTH("30", stealthValidation, stealthToFunctional, stealthToDisplay, ) // Await confirmation from the organisers who use AV that this is a skill they want
}


interface HackerRepo : CrudRepository<Hacker, String> {
    fun findByHackerUserId(hackerUserId: String): Hacker?
}
