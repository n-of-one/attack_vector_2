package org.n1.av2.backend.service.layerhacking.service

import org.n1.av2.backend.engine.CalledBySystem
import org.n1.av2.backend.engine.ScheduledTask
import org.n1.av2.backend.engine.SystemTaskRunner
import org.n1.av2.backend.entity.run.HackerActivity
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.service.Timer
import org.n1.av2.backend.entity.service.TimerEntityService
import org.n1.av2.backend.entity.service.TimerType
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.other.TripwireLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.site.toDuration
import org.n1.av2.backend.service.terminal.TERMINAL_MAIN
import org.springframework.context.annotation.Lazy
import java.time.Duration
import java.time.ZonedDateTime

class TimerInfo(val timerId: String, val finishAt: ZonedDateTime, val type: TimerType, val target: String, val effect: String)


@org.springframework.stereotype.Service
class TripwireLayerService(
    private val stompService: StompService,
    private val time: TimeService,
    private val systemTaskRunner: SystemTaskRunner,
    private val timerEntityService: TimerEntityService,
    private val nodeEntityService: NodeEntityService,
    @Lazy private val runService: RunService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val siteService: SiteService,
) {


    fun hack(layer: Layer) {
        stompService.replyTerminalReceive("Hack failed. Countdown timer is not managed from service.")
    }

    fun connect(layer: Layer) {
        stompService.replyTerminalReceive("Service has no UI.")
    }

    fun hackerArrivesNode(siteId: String, layer: TripwireLayer, nodeId: String, userId: String, runId: String) {
        val existingDetection = timerEntityService.findByLayer(layer.id)
        if (existingDetection != null) {
            return
        }

        val duration = layer.countdown.toDuration("tripwire ${layer.id}")
        val shutdownTime = time.now().plus(duration)

        val timer = timerEntityService.create(layer.id, null, shutdownTime, siteId, siteId, TimerType.SHUTDOWN_START)

        stompService.toSite(siteId, ServerActions.SERVER_START_TIMER, toTimerInfo(timer, layer))
        stompService.replyTerminalReceive("[pri]${layer.level}[/] Tripwire triggered [warn]site reset[/] in ${toDurationString(duration)}[/].")

        stompService.toRun(runId, ServerActions.SERVER_FLASH_PATROLLER, "nodeId" to nodeId)

        systemTaskRunner.queueInSeconds(siteId, duration.seconds) { timerActivates(siteId, timer.id, layer) }
    }

    @ScheduledTask
    @CalledBySystem
    fun timerActivates(siteId: String, timerId: String, layer: TripwireLayer) {
        stompService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to timerId)
        timerEntityService.deleteById(timerId)

        val hackerStates = hackerStateEntityService.findAllHackersInSite(siteId)
        siteService.resetSite(siteId, layer.shutdown)
        hackerStates
            .filter { it.activity == HackerActivity.ATTACKING }
            .forEach { hackerState ->
                runService.hackerDisconnect(hackerState, "Disconnected (server abort)")
            }

        val shutdownDuration = layer.shutdown.toDuration("shutdown")
        val shutdownEndTime = time.now().plus(shutdownDuration)

        val shutdownTimer = timerEntityService.create(layer.id, null, shutdownEndTime, siteId, siteId, TimerType.SHUTDOWN_FINISH)
        stompService.toSite(siteId, ServerActions.SERVER_START_TIMER, toTimerInfo(shutdownTimer, layer))

        siteService.shutdownSite(siteId, shutdownEndTime)
        systemTaskRunner.queueInSeconds(siteId, shutdownDuration.seconds) { shutdownFinished(siteId, shutdownTimer.id) }
    }

    @ScheduledTask
    @CalledBySystem
    fun shutdownFinished(siteId: String, shutdownTimerId: String) {
        siteService.shutdownFinished(siteId)
        timerEntityService.deleteById(shutdownTimerId)
        stompService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to shutdownTimerId)
        stompService.toSite(siteId, ServerActions.SERVER_TERMINAL_RECEIVE, StompService.TerminalReceive(TERMINAL_MAIN, arrayOf("[info]Site connection available again.")))
    }


    private fun toDurationString(duration: Duration): String {
        if (duration.toHoursPart() > 0) {
            return "%02d:%02d:%02d".format(duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart())
        }
        return "%02d:%02d".format(duration.toMinutesPart(), duration.toSecondsPart())
    }

    fun findForEnterSite(siteId: String, userId: String): List<TimerInfo> {
        val timers = timerEntityService.findForEnterSite(siteId, userId)

        val countdowns = timers.map { timer ->
            val layer = nodeEntityService.findLayer(timer.layerId) as TripwireLayer
            toTimerInfo(timer, layer)
        }

        return countdowns
    }

    fun toTimerInfo(timer: Timer, layer: TripwireLayer): TimerInfo {
        return TimerInfo(timer.id, timer.finishAt, timer.type, "site", determineEffect(timer.type, layer.shutdown))
    }

    private fun determineEffect(type: TimerType, shutdownTime: String): String {
        return when (type) {
            TimerType.SHUTDOWN_START -> "shutdown in ${shutdownTime})"
            TimerType.SHUTDOWN_FINISH -> "site available"
            else -> "Unknown"
        }
    }


}