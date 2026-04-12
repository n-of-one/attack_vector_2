package org.n1.av2.platform.iam.user

import org.n1.av2.hacker.skill.Skill


enum class UIUserType {
    HACKER,
    GM,
    ADMIN,
    SYSTEM,
}

@Suppress("unused")
class UserOverview(
    val id: String,
    val name: String,
    val characterName: String? = null,
    val hacker: Boolean,
    val type: UIUserType,
    val tag: UserTag,
)

@Suppress("unused")
class UiHacker(
    val hackerUserId: String,
    val icon: HackerIcon,
    val characterName: String,
    val skills: List<Skill>,
    val scriptCredits: Int,
    val scriptIncomeCollectionStatus: ScriptIncomeCollectionStatus,
)

class UiUserDetails(
    val id: String,
    val name: String,
    val type: UserType,
    val preferences: UserPreferences,
    val hacker: UiHacker?,
)

enum class ScriptIncomeCollectionStatus {
    HACKER_HAS_NO_INCOME,
    TODAY_IS_NOT_AN_INCOME_DATE,
    AVAILABLE,
    COLLECTED
}
