package org.n1.av2.backend.engine

import org.n1.av2.backend.service.layer.TimerActivatesGameEvent
import org.n1.av2.backend.service.layer.TimerTriggerLayerService
import org.n1.av2.backend.service.patroller.TracingPatrollerArrivesGameEvent
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.n1.av2.backend.service.patroller.TracingPatrollerSnappedBackHackerGameEvent
import org.n1.av2.backend.service.run.HackingService
import org.n1.av2.backend.service.run.StartAttackArriveGameEvent
import org.n1.av2.backend.service.terminal.hacking.ArriveProbeLayersGameEvent
import org.n1.av2.backend.service.terminal.hacking.CommandMoveService
import org.n1.av2.backend.service.terminal.hacking.MoveArriveGameEvent
import org.springframework.stereotype.Service

@Service
class GameEventService(
        private val timerTriggerLayerService: TimerTriggerLayerService,
        private val tracingPatrollerService: TracingPatrollerService,
        private val hackingService: HackingService,
        private val commandMoveService: CommandMoveService
) {

    fun run(event: GameEvent) {
        when (event) {
            is TimerActivatesGameEvent -> timerTriggerLayerService.timerActivates(event)
            is TracingPatrollerArrivesGameEvent -> tracingPatrollerService.patrollerArrives(event)
            is MoveArriveGameEvent -> commandMoveService.moveArrive(event)
            is ArriveProbeLayersGameEvent -> commandMoveService.probedLayers(event)
            is StartAttackArriveGameEvent -> hackingService.startAttackArrive(event)
            is TracingPatrollerSnappedBackHackerGameEvent -> tracingPatrollerService.processLockHacker(event)

            else -> error("don't know how to process " + event.javaClass)
        }
    }
}