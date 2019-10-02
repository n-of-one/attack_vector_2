package org.n1.av2.backend.engine

import java.security.Principal

class Task(val action: () -> Unit, val principal: Principal)

data class TimedEvent(val due: Long, val event: GameEvent)

interface GameEvent

class SnifferAlarmEvent(val runId: String, val nodeId: String) : GameEvent