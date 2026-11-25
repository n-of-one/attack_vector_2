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

enum class UplinkActivity {
    NONE,
    CONNECTING, // up
    RETURNING, // down, to the uplink node
    RETURNING_OUTSIDE, // down, the uplink node cannot be used: outside the site. For example: the site is resetting, or a network switch cut off the uplink node
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
    val iceId: String?, // the ICE a hacker is currently hacking
    val networkedConnectionId: String?,
    val iceConnectTimestamp: ZonedDateTime?,
    val ingressNodeId: String? = null, // the node where the hacker connected to the site. Network switches cut off hackers that can't be reached from it.
    val uplinkReturns: List<UplinkReturn> = emptyList(), // where to return to when leaving a site entered via an uplink, last is most recent
    val uplinkReturnNodeId: String? = null, // the uplink node to return to, set while the browser switches back to the run
    val uplinkActivity: UplinkActivity = UplinkActivity.NONE,
) {

    fun toRunState(): HackerStateRunning {
        return HackerStateRunning(
            userId, connectionId,
            runId ?: error("runId null for ${userId}"),
            siteId ?: error("siteId null for ${userId}"),
            currentNodeId,
            previousNodeId, activity, iceId, networkedConnectionId, iceConnectTimestamp,
            ingressNodeId, uplinkReturns, uplinkReturnNodeId, uplinkActivity,
        )
    }
}

data class UplinkReturn(
    val runId: String,
    val siteId: String,
    val nodeId: String, // the uplink node
    val ingressNodeId: String, // the node where the hacker originally connected to this site
)

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
    val ingressNodeId: String?,
    val uplinkReturns: List<UplinkReturn>,
    val uplinkReturnNodeId: String?,
    val uplinkActivity: UplinkActivity,
) {
    fun toState(): HackerState {
        return HackerState(
            userId, connectionId, runId, siteId, currentNodeId, previousNodeId,
            activity, iceId, networkedConnectionId, iceConnectTimestamp, ingressNodeId, uplinkReturns, uplinkReturnNodeId, uplinkActivity,
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
