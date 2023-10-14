package org.n1.av2.backend.engine

import org.n1.av2.backend.model.Timings


interface GameEvent

open class TicksGameEvent(val timings: Timings): GameEvent

class SnifferAlarmEvent(val runId: String, val nodeId: String) : GameEvent

/**
 * Marker annotation to indicate that a method will be called as a Scheduled task.
 * This emphasized that the game state might have changed since this methos was 'called'.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ScheduledTask