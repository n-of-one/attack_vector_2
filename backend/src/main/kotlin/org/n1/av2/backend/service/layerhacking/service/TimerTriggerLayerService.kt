package org.n1.av2.backend.service.layerhacking.service

import org.n1.av2.backend.engine.GameEvent
import org.n1.av2.backend.engine.ScheduledTask
import org.n1.av2.backend.engine.SystemTaskRunner
import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.other.TimerTriggerLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


class TimerActivatesGameEvent(val runId: String, val nodeId: String, val userId: String): GameEvent


@Service
class TimerTriggerLayerService(
    private val systemTaskRunner: SystemTaskRunner,
    private val time: TimeService,
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val tracingPatrollerService: TracingPatrollerService) {


    fun hack(layer: TimerTriggerLayer) {
        stompService.replyTerminalReceive("${layer.name} resists hack.",
                "Analysis: this layer will monitor network traffic to find irregularities. It will detect your persona's mask in ${detectTime(layer)}. " +
                "Then it will launch a patroller to lock your persona and will attempt to trace back your network origin.")
    }

    private class CountdownStart(val finishAt: ZonedDateTime)

    fun hackerArrivesNode(siteId: String, layer: TimerTriggerLayer, nodeId: String, userId: String, runId: String) {

        val alarmTime = time.now()
                .plusMinutes(layer.minutes)
                .plusSeconds(layer.seconds)
        stompService.reply(ServerActions.SERVER_START_COUNTDOWN, CountdownStart(alarmTime))

        stompService.replyTerminalReceive("[pri]${layer.level}[/] Network sniffer : analyzing traffic. Persona mask will fail in [error]${detectTime(layer)}[/].")

        class FlashPatroller(val nodeId: String)
        stompService.toRun(runId, ServerActions.SERVER_FLASH_PATROLLER, FlashPatroller(nodeId))

        val event = TimerActivatesGameEvent(runId, nodeId, userId)
        val timerSeconds = layer.minutes * 60 + layer.seconds
        systemTaskRunner.queueInSeconds(siteId, timerSeconds) { timerActivates(event) }
    }

    private fun detectTime(layer: TimerTriggerLayer): String {
        return "%02d:%02d".format(layer.minutes, layer.seconds)
    }

    @ScheduledTask
    fun timerActivates(event: TimerActivatesGameEvent) {
        stompService.toRun(event.runId, ServerActions.SERVER_COMPLETE_COUNTDOWN)
        tracingPatrollerService.activatePatroller(event.nodeId, event.userId, event.runId)
    }

    fun connect(layer: TimerTriggerLayer) {
        stompService.replyTerminalReceive("Access to UI denied")
    }
}