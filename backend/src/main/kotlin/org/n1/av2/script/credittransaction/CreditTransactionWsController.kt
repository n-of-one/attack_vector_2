package org.n1.av2.script.credittransaction

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.UserType
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class CreditTransactionWsController(
    private val userTaskRunner: UserTaskRunner,
    private val creditTransactionService: CreditTransactionService,
    ) {

    @MessageMapping("/hacker/creditTransaction/send")
    fun sendTransactionsForCurrentUser(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/hacker/creditTransaction/send", userPrincipal) {
            creditTransactionService.sendTransactionsForUser(userPrincipal.userId)
        }
    }

    @MessageMapping("/gm/creditTransaction/send")
    fun sendTransactionsForUser(userId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/creditTransaction/send", userPrincipal) {
            if (userPrincipal.userEntity.type != UserType.GM) {
                throw IllegalArgumentException("You are not a GM")
            }
            creditTransactionService.sendTransactionsForUser(userId)
        }
    }

    data class GmAdjustCreditsCommand(val userId: String, val amount: Int, val description: String)
    @MessageMapping("/gm/scriptCredits/adjust")
    fun gmAdjustCredits(command: GmAdjustCreditsCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/gm/scriptCredits/adjust", userPrincipal) {
            if (userPrincipal.userEntity.type != UserType.GM) {
                throw IllegalArgumentException("You are not a GM")
            }
            creditTransactionService.gmAdjust(command.userId, command.amount, command.description)
        }
    }

}
