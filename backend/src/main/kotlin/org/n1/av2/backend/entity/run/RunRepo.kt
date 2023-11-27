package org.n1.av2.backend.entity.run

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RunRepo : CrudRepository<Run, String> {
    fun findByRunIdIn(runIds: List<String>): List<Run>
    fun findBySiteId(siteId: String): List<Run>
    fun findByRunId(runId: String): Run?
}

@Repository
interface UserRunLinkRepo : CrudRepository<UserRunLink, String> {
    fun findAllByUserId(userId:String): List<UserRunLink>
    fun findAllByRunId(runId: String): List<UserRunLink>
    fun findByUserIdAndRunId(userId: String, runId: String): UserRunLink?
    fun deleteByUserIdAndRunId(userId: String, runId: String)
    fun deleteAllByRunId(runId: String)
    fun deleteAllByUserId(userId: String)
}
