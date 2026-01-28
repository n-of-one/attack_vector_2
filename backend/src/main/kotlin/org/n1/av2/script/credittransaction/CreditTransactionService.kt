package org.n1.av2.script.credittransaction

import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions.SERVER_RECEIVE_CREDITS_TRANSACTIONS
import org.n1.av2.platform.iam.user.GM_ADJUST_CREDITS_USER
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.platform.util.TimeService
import org.n1.av2.platform.util.createId
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class CreditTransactionService(
    private val creditTransactionRepo: CreditTransactionRepo,
    private val timeService: TimeService,
    private val connectionService: ConnectionService,
    private val hackerEntityService: HackerEntityService,
    private val userEntityService: UserEntityService,
    private val userAndHackerService: UserAndHackerService,
) {

    private val logger = mu.KotlinLogging.logger {}

    data class UiCreditTransaction(
        val fromUserName: String,
        val fromUserType: UserType,
        val toUserName: String,
        val toUserType: UserType,
        val amount: Int,
        val description: String,
        val timestamp: ZonedDateTime,
    )

    fun sendTransactionsForUser(userId: String, toUserId: String? = null) {
        val transactions = creditTransactionRepo.findByFromUserIdOrToUserId(userId, userId)
        val uiTransactions = transactions.map {

            val fromUser = userEntityService.getById(it.fromUserId)
            val toUser = userEntityService.getById(it.toUserId)
            UiCreditTransaction(
                fromUserName = fromUser.name,
                toUserName = toUser.name,
                fromUserType = fromUser.type,
                toUserType = toUser.type,
                amount = it.amount,
                description = it.description,
                timestamp = it.timestamp,
            )
        }
        if (toUserId == null) {
            connectionService.reply(SERVER_RECEIVE_CREDITS_TRANSACTIONS, uiTransactions)
        }
        else {
            connectionService.toUser(toUserId, SERVER_RECEIVE_CREDITS_TRANSACTIONS, uiTransactions)
        }
    }

    fun transferCredits(fromUserId: String, toUserId: String, amount: Int, description: String) {
        val fromUser = userEntityService.getById(fromUserId)
        if (fromUser.type == UserType.HACKER) {
            val sender = hackerEntityService.findForUserId(fromUserId)
            val newBalance = sender.scriptCredits - amount
            if (newBalance < 0) error("Sender does not have enough script credits. Current balance: ${sender.scriptCredits}, attempted to send: $amount")
            hackerEntityService.save(sender.copy(
                scriptCredits = newBalance
            ))
        }

        val toUser = userEntityService.getById(toUserId)
        if (toUser.type == UserType.HACKER) {
            val receiver = hackerEntityService.findForUserId(toUserId)
            val newBalance = receiver.scriptCredits + amount
            hackerEntityService.save(receiver.copy(
                scriptCredits = newBalance
            ))
        }

        val transaction = CreditTransaction(
            id = createId("transaction", creditTransactionRepo::findById),
            fromUserId = fromUserId,
            toUserId = toUserId,
            amount = amount,
            description = description,
            timestamp = timeService.now()
        )
        creditTransactionRepo.save(transaction)

        logger.info("Transfer $amount ⚡ from ${fromUser.name} to ${toUser.name}. Description: $description")
    }

    fun gmAdjust(userId: String, amount: Int, description: String) {
        if (amount > 0) {
            transferCredits(GM_ADJUST_CREDITS_USER.id, userId, amount, description)
        }
        else {
            transferCredits(userId, GM_ADJUST_CREDITS_USER.id, -amount, description)
        }
        sendTransactionsForUser(userId)
        userAndHackerService.sendDetailsOfSpecificUser(userId)
    }
}
