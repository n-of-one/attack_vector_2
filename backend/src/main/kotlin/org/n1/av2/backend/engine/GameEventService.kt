package org.n1.av2.backend.engine

import org.n1.av2.backend.service.layer.SnifferDetectsHackerGameEvent
import org.n1.av2.backend.service.layer.SnifferLayerService
import org.n1.av2.backend.service.layer.SnifferPatrollerArriveGameEvent
import org.springframework.stereotype.Service

@Service
class GameEventService(
        val alarmService: SnifferLayerService
) {

    fun run(event: GameEvent) {
        when (event) {
            is SnifferDetectsHackerGameEvent -> alarmService.processAlarm(event)
            is SnifferPatrollerArriveGameEvent -> alarmService.processPatrollerArrive(event)

            else -> error("don't know how to process " + event.javaClass)
        }
    }
}