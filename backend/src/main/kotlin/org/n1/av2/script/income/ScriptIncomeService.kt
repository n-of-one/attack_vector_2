package org.n1.av2.script.income

import org.n1.av2.hacker.skill.SkillRepo
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions.SERVER_RECEIVE_INCOME_DATES
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.SCRIPT_INCOME_USER
import org.n1.av2.platform.iam.user.ScriptIncomeCollectionStatus
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.util.TimeService
import org.n1.av2.platform.util.createId
import org.n1.av2.script.credittransaction.CreditTransactionService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ScriptIncomeService(
    private val scriptIncomeDateRepository: ScriptIncomeDateRepository,
    private val connectionService: ConnectionService,
    private val timeService: TimeService,
    private val hackerSkillRepo: SkillRepo,
    private val userEntityService: UserEntityService,
    private val currentUserService: CurrentUserService,
    private val creditTransactionService: CreditTransactionService,
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

    fun collect() {
        val allIncomeDates = scriptIncomeDateRepository.findAll()
        val incomeDateToday = allIncomeDates.find { timeService.currentPayoutDate() == it.date} ?: error("No income date for today found.")

        if (incomeDateToday.collectedByUserIds.contains(currentUserService.userId)) error("You have already collected your script credits for today.")

        val incomeSkill =
            hackerSkillRepo.findByUserId(currentUserService.userId).find { it.type == SkillType.SCRIPT_CREDITS } ?: error("You don't access to script credits")
        val incomeValue = incomeSkill.value?.toIntOrNull() ?: 0
        if (incomeValue <= 0) error("You don't access to script credits income")

        creditTransactionService.transferCredits(SCRIPT_INCOME_USER.id, incomeSkill.userId, incomeValue, "Income")
        creditTransactionService.sendTransactionsForUser(incomeSkill.userId)

        val updatedIncomeDate = incomeDateToday.copy(
            collectedByUserIds = incomeDateToday.collectedByUserIds + currentUserService.userId
        )
        scriptIncomeDateRepository.save(updatedIncomeDate)
    }


    private data class UiScriptIncomeDate(
        val id: ScriptIncomeDateId,
        val date: LocalDate,
        val collectedByUserNames: List<String>,
        val status: IncomeDateStatus,
    )

    private enum class IncomeDateStatus {
        PAST,
        COLLECTABLE,
        SCHEDULED
    }

    fun sendAllIncomeDates() {
        val currentPaymentDate = timeService.currentPayoutDate()
        val uiIncomeDates = scriptIncomeDateRepository
            .findAll()
            .sortedBy { it.date }
            .map {
                UiScriptIncomeDate(
                    id = it.id,
                    date = it.date,
                    collectedByUserNames = it.collectedByUserIds.map { userId -> userEntityService.getById(userId).name },
                    status = when {
                        it.date.isBefore(currentPaymentDate) -> IncomeDateStatus.PAST
                        it.date == currentPaymentDate -> IncomeDateStatus.COLLECTABLE
                        else -> IncomeDateStatus.SCHEDULED
                    }
                )
            }
        connectionService.reply(SERVER_RECEIVE_INCOME_DATES, uiIncomeDates)
    }

    fun addIncomeDateRange(start: LocalDate, end: LocalDate) {
        if (end.isBefore(start)) error("end date must be after start date")
        start.datesUntil(end.plusDays(1)).forEach { date -> addIncomeDate(date) }
    }

    private fun addIncomeDate(date: LocalDate) {
        if (scriptIncomeDateRepository.findAll().any { it.date == date }) {
            error("Income date for $date already exists")
        }
        val id = createId("incomeDate", scriptIncomeDateRepository::findById)
        val newIncomeDate = ScriptIncomeDate(id, date, emptyList())
        scriptIncomeDateRepository.save(newIncomeDate)
        sendAllIncomeDates()
    }

    fun deleteIncomeDate(id: ScriptIncomeDateId) {
        scriptIncomeDateRepository.deleteById(id)
        sendAllIncomeDates()
    }

}
