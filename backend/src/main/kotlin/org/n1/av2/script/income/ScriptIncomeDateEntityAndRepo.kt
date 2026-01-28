package org.n1.av2.script.income

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

typealias ScriptIncomeDateId = String

@Document
data class ScriptIncomeDate(
    @Id val id: ScriptIncomeDateId,
    val date: LocalDate,
    val collectedByUserIds: List<String>,
)

@Repository
interface ScriptIncomeDateRepository : MongoRepository<ScriptIncomeDate, String>
