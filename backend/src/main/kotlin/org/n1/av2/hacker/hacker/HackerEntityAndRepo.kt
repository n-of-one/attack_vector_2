package org.n1.av2.hacker.hacker

import org.n1.av2.platform.iam.user.HackerIcon
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import java.time.ZonedDateTime

@Document
data class Hacker(
    @Id val id: String,
    val hackerUserId: String,
    val icon: HackerIcon,
    val characterName: String,
    val scriptCredits: Int = 0,
    val lastReceivedScriptCreditsIncome: ZonedDateTime? = null,
)

interface HackerRepo : CrudRepository<Hacker, String> {
    fun findByHackerUserId(hackerUserId: String): Hacker?
}
