package org.n1.av2.backend.engine

import org.n1.av2.backend.service.run.AlarmGameEvent
import org.n1.av2.backend.service.run.AlarmService
import org.springframework.stereotype.Service

@Service
class GameEventService(
        val alarmService: AlarmService
) {

    fun run(event: GameEvent) {
        when (event) {
            is AlarmGameEvent -> alarmService.processAlarm(event)
            else -> error("don't know how to process " + event.javaClass)
        }
    }
}