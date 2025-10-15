package org.n1.av2.script.income

import org.n1.av2.hacker.skill.SkillRepo
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.platform.iam.user.ScriptIncomeCollectionStatus
import org.n1.av2.platform.util.TimeService
import org.springframework.stereotype.Service

@Service
class ScriptIncomeEntityService (
    private val scriptIncomeDateRepository: ScriptIncomeDateRepository,
    private val timeService: TimeService,
    private val hackerSkillRepo: SkillRepo,
) {

    fun scriptIncomeCollectionStatus(userId: String): ScriptIncomeCollectionStatus {
        val incomeSkill =
            hackerSkillRepo.findByUserId(userId).find { it.type == SkillType.SCRIPT_CREDITS } ?: return ScriptIncomeCollectionStatus.HACKER_HAS_NO_INCOME
        val incomeValue = incomeSkill.value?.toIntOrNull() ?: 0
        if (incomeValue == 0) return ScriptIncomeCollectionStatus.HACKER_HAS_NO_INCOME

        val allIncomeDates = scriptIncomeDateRepository.findAll()
        val incomeDateToday = allIncomeDates.find { timeService.currentPayoutDate() == it.date} ?: return ScriptIncomeCollectionStatus.TODAY_IS_NOT_AN_INCOME_DATE

        if (incomeDateToday.collectedByUserIds.contains(userId)) return ScriptIncomeCollectionStatus.COLLECTED

        return ScriptIncomeCollectionStatus.AVAILABLE
    }

}
