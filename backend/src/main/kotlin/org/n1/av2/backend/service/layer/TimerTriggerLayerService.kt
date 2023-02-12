package org.n1.av2.backend.service.layer

import org.n1.av2.backend.engine.GameEvent
import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.entity.site.layer.TimerTriggerLayer
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


class TimerActivatesGameEvent(val runId: String, val nodeId: String, val userId: String): GameEvent


@Service
class TimerTriggerLayerService(
    val taskRunner: TaskRunner,
    val time: TimeService,
    val stompService: StompService,
    val tracingPatrollerService: TracingPatrollerService) {


    fun hack(layer: TimerTriggerLayer) {
        stompService.terminalReceiveCurrentUser("${layer.name} resists hack.",
                "Analysis: this layer will monitor network traffic to find irregularities. It will detect your persona's mask in ${detectTime(layer)}. " +
                "Then it will launch a patroller to lock your persona and will attempt to trace back your network origin.")
    }

    private class CountdownStart(val finishAt: ZonedDateTime)

    fun hackerTriggers(layer: TimerTriggerLayer, nodeId: String, userId: String, runId: String) {

        val alarmTime = time.now()
                .plusMinutes(layer.minutes)
                .plusSeconds(layer.seconds)
        stompService.toUser(userId, ReduxActions.SERVER_START_COUNTDOWN, CountdownStart(alarmTime))

        stompService.terminalReceiveForUser(userId, "[pri]${layer.level}[/] Network sniffer : analyzing traffic. Persona mask will fail in [error]${detectTime(layer)}[/].")

        class FlashPatroller(val nodeId: String)
        stompService.toRun(runId, ReduxActions.SERVER_FLASH_PATROLLER, FlashPatroller(nodeId))

        val event = TimerActivatesGameEvent(runId, nodeId, userId)
        taskRunner.queueInMinutesAndSeconds(layer.minutes, layer.seconds) { timerActivates(event) }
    }

    private fun detectTime(layer: TimerTriggerLayer): String {
        return "%02d:%02d".format(layer.minutes, layer.seconds)
    }

    fun timerActivates(event: TimerActivatesGameEvent) {
        stompService.toRun(event.runId, ReduxActions.SERVER_COMPLETE_COUNTDOWN)
        tracingPatrollerService.activatePatroller(event.nodeId, event.userId, event.runId)
    }
}