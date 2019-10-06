package org.n1.av2.backend.engine

import org.n1.av2.backend.service.layer.TimerActivatesGameEvent
import org.n1.av2.backend.service.layer.TimerTriggerLayerService
import org.n1.av2.backend.service.patroller.TracingPatrollerArrivesGameEvent
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.springframework.stereotype.Service

@Service
class GameEventService(
        val timerTriggerLayerService: TimerTriggerLayerService,
        val tracingPatrollerService: TracingPatrollerService
) {

    fun run(event: GameEvent) {
        when (event) {
            is TimerActivatesGameEvent -> timerTriggerLayerService.timerActivates(event)
            is TracingPatrollerArrivesGameEvent -> tracingPatrollerService.patrollerArrives(event)

            else -> error("don't know how to process " + event.javaClass)
        }
    }
}