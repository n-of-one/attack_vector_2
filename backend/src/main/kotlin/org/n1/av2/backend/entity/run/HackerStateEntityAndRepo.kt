package org.n1.av2.backend.entity.run

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


enum class HackerActivity(val inRun: Boolean) {
    OFFLINE(false),    // hacker is not online
    ONLINE(false),     // hacker is online, but not in a run

    SCANNING(true),   // hacker has approached a site, scanning from a distance
    ATTACKING(true),    // hacker has entered the site and is in a run
}

@Document
data class HackerState(
    @Id val userId: String,
    val connectionId: String,
    val runId: String?,
    val siteId: String?,
    val currentNodeId: String?,
    val previousNodeId: String?,
    val activity: HackerActivity,
    val masked: Boolean
) {

    fun toRunState(): HackerStateRunning {
        return HackerStateRunning(
            userId, connectionId,
            runId ?: error("runId null for ${userId}"),
            siteId ?: error("siteId null for ${userId}"),
            currentNodeId ?: error("currentNodeId null for ${userId}"),
            previousNodeId, masked
        )
    }
}

/** Convenience class that mimicks HackerState but enforces non-null state of all fields that are used in a run */
class HackerStateRunning(
    val userId: String,
    val connectionId: String,
    val runId: String,
    val siteId: String,
    val currentNodeId: String,
    val previousNodeId: String?,
    val masked: Boolean
) {
    fun toState(): HackerState {
        return HackerState(
            userId, connectionId, runId, siteId, currentNodeId, previousNodeId,
            HackerActivity.ATTACKING, masked
        )
    }
}

@Repository
interface HackerStateRepo : CrudRepository<HackerState, String> {
    fun findByUserId(userId: String): HackerState?
    fun findByRunId(runId: String):List<HackerState>
    fun findBySiteId(siteId: String):List<HackerState>
}