package org.n1.av2.script.credittransaction

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime


@Document
data class CreditTransaction(
    @Id
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val amount: Int,
    val description: String,
    val timestamp: ZonedDateTime,
)

@Repository
interface CreditTransactionRepo : org.springframework.data.mongodb.repository.MongoRepository<CreditTransaction, String> {
    fun findByFromUserIdOrToUserId(fromUserId: String, toUserId: String): List<CreditTransaction>
}
