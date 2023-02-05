package org.n1.av2.backend.engine

import org.n1.av2.backend.model.Ticks


interface GameEvent

open class TicksGameEvent(val ticks: Ticks): GameEvent

class SnifferAlarmEvent(val runId: String, val nodeId: String) : GameEvent