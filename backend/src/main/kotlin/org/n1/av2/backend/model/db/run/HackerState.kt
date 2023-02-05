package org.n1.av2.backend.model.db.run

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

enum class HackerGeneralActivity {
    OFFLINE,    // this hacker is not online
    ONLINE,     // this hacker is online, but not in a run
    RUNNING,    // this hacker is in a run
}

enum class RunActivity {
    NA,         // not in a run
    SCANNING,   // hacker has not yet started the attack
    STARTING,   // hacker is starting the attack (moving to the start node)
    AT_NODE,    // hacker is at rest at a node
    MOVING,  // TODO Remove
}

@Document
data class HackerState(
        @Id val userId: String,
        val runId: String?,
        val siteId: String?,
        val currentNodeId: String?,
        val previousNodeId: String?,
        val targetNodeId: String?,
        val generalActivity: HackerGeneralActivity,
        val runActivity: RunActivity,
        val hookPatrollerId: String?, // Hooked means that a patroller has either locked the hacker, is about to lock the hacker and they cannot escape.
        val locked: Boolean
) {

    fun toRunState(): HackerStateRunning {
        return HackerStateRunning(userId,
                runId ?: error("runId null for ${userId}"),
                siteId ?: error("siteId null for ${userId}"),
                currentNodeId ?: error("currentNodeId null for ${userId}"),
                previousNodeId,
                targetNodeId,
                runActivity,
                hookPatrollerId,
                locked
        )
    }
}

/** Convenience class that mimicks HackerState but enforces non-null state of all fields that are used in a run */
class HackerStateRunning(
        val userId: String,
        val runId: String,
        val siteId: String,
        val currentNodeId: String,
        val previousNodeId: String?,
        val targetNodeId: String?,
        val runActivity: RunActivity,
        val hookPatrollerId: String?,
        val locked: Boolean) {

    fun toState(): HackerState {
        return HackerState(userId, runId, siteId, currentNodeId, previousNodeId, targetNodeId,
                HackerGeneralActivity.RUNNING, runActivity, hookPatrollerId, locked)
    }
}