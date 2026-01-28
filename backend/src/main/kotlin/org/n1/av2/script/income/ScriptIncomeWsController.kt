package org.n1.av2.script.income

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import java.time.LocalDate

@Validated
@Controller
class ScriptIncomeWsController(
    private val userTaskRunner: UserTaskRunner,
    private val scriptIncomeService: ScriptIncomeService,
    private val userAndHackerService: UserAndHackerService,
) {

    @MessageMapping("/hacker/scriptCredits/collect")
    fun collect(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/hacker/scriptCredits/collect", userPrincipal) {
            scriptIncomeService.collect()
            userAndHackerService.sendDetailsOfCurrentUser()
        }
    }

    @MessageMapping("/gm/scriptIncome/sendDates")
    fun sendDates(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptIncome/sendDates", userPrincipal) { scriptIncomeService.sendAllIncomeDates() }
    }

    data class CommandAddScriptIncomeDateRange(val start: LocalDate, val end: LocalDate)

    @MessageMapping("/gm/scriptIncome/add")
    fun add(command: CommandAddScriptIncomeDateRange, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptIncome/add", userPrincipal) { scriptIncomeService.addIncomeDateRange(command.start, command.end) }
    }

    @MessageMapping("/gm/scriptIncome/delete")
    fun delete(id: ScriptIncomeDateId, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptIncome/delete", userPrincipal) { scriptIncomeService.deleteIncomeDate(id) }
    }

}
