package org.n1.av2.backend.engine

import org.n1.av2.backend.model.Timings


interface GameEvent

open class TicksGameEvent(val timings: Timings): GameEvent

class SnifferAlarmEvent(val runId: String, val nodeId: String) : GameEvent