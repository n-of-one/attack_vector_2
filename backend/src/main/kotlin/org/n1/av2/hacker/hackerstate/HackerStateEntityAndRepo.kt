package org.n1.av2.hacker.hackerstate

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime


enum class HackerActivity(val inRun: Boolean) {
    OFFLINE(false),    // hacker is not online
    ONLINE(false),     // hacker is online, but not in a run

    OUTSIDE(true),   // hacker has approached a site, has a connection but not yet an avatar inside
    INSIDE(true),    // hacker has entered the site, has an avatar inside
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
    val iceId: String?,
    val networkedConnectionId: String?,
    val iceConnectTimestamp: ZonedDateTime?,
) {

    fun toRunState(): HackerStateRunning {
        return HackerStateRunning(
            userId, connectionId,
            runId ?: error("runId null for ${userId}"),
            siteId ?: error("siteId null for ${userId}"),
            currentNodeId,
            previousNodeId, activity, iceId, networkedConnectionId, iceConnectTimestamp,

        )
    }
}

/** Convenience class that mimics HackerState but enforces non-null state of all fields that are used in a run */
class HackerStateRunning(
    val userId: String,
    val connectionId: String,
    val runId: String,
    val siteId: String,
    val currentNodeId: String?,
    val previousNodeId: String?,
    val activity: HackerActivity,
    val iceId: String?,
    val networkedConnectionId: String?,
    val iceConnectTimestamp: ZonedDateTime?,
) {
    fun toState(): HackerState {
        return HackerState(
            userId, connectionId, runId, siteId, currentNodeId, previousNodeId,
            activity, iceId, networkedConnectionId, iceConnectTimestamp
        )
    }
}

@Repository
interface HackerStateRepo : CrudRepository<HackerState, String> {
    fun findByRunId(runId: String): List<HackerState>
    fun findBySiteId(siteId: String): List<HackerState>
    fun findByIceId(iceId: String): List<HackerState>
    fun findByRunIdAndIceId(runId: String, networkedAppId: String): List<HackerState>
}
