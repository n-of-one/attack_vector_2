package org.n1.av2.run.runlink

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


@Document
data class RunLink(
    @Id val _mongoId_: String? = null,
    @Indexed val userId: String,
    @Indexed val runId: String
)

@Repository
interface RunLinkRepo : CrudRepository<RunLink, String> {
    fun findAllByUserId(userId:String): List<RunLink>
    fun findAllByRunId(runId: String): List<RunLink>
    fun findByUserIdAndRunId(userId: String, runId: String): RunLink?
    fun deleteByUserIdAndRunId(userId: String, runId: String)
    fun deleteAllByRunId(runId: String)
    fun deleteAllByUserId(userId: String)
}
