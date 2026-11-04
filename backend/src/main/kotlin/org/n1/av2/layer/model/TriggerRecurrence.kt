package org.n1.av2.layer.model

/**
 * How often a layer that reacts to a hacker entering its node is triggered (e.g. Timer adjuster, Daemon).
 */
enum class TriggerRecurrence {
    FIRST_ENTRY_ONLY, // only for the first hacker that triggers it
    EVERY_ENTRY,
    EACH_HACKER_ONCE;

    /**
     * @param triggeredUserIds the users that already triggered the layer since the last site reset.
     */
    fun triggers(triggeredUserIds: List<String>, userId: String): Boolean {
        return when (this) {
            FIRST_ENTRY_ONLY -> triggeredUserIds.isEmpty()
            EVERY_ENTRY -> true
            EACH_HACKER_ONCE -> !triggeredUserIds.contains(userId)
        }
    }
}
