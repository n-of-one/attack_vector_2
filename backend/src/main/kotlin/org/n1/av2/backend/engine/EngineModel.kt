package org.n1.av2.backend.engine

import org.n1.av2.backend.model.Ticks
import org.n1.av2.backend.model.db.user.User

class Task(val action: () -> Unit, val user: User)

data class TimedEvent(val omniId: String, val due: Long, val event: GameEvent)

interface GameEvent

open class TicksGameEvent(val ticks: Ticks): GameEvent

class SnifferAlarmEvent(val runId: String, val nodeId: String) : GameEvent