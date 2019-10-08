package org.n1.av2.backend.engine

import org.n1.av2.backend.model.Ticks
import java.security.Principal

class Task(val action: () -> Unit, val principal: Principal)

data class TimedEvent(val due: Long, val event: GameEvent)

interface GameEvent

open class TicksGameEvent(val ticks: Ticks): GameEvent

class SnifferAlarmEvent(val runId: String, val nodeId: String) : GameEvent