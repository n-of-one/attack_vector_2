package org.n1.av2.backend.service.layerhacking.service

import org.n1.av2.backend.engine.CalledBySystem
import org.n1.av2.backend.engine.ScheduledTask
import org.n1.av2.backend.engine.SystemTaskRunner
import org.n1.av2.backend.engine.TaskIdentifiers
import org.n1.av2.backend.entity.run.HackerActivity
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.service.Timer
import org.n1.av2.backend.entity.service.TimerEntityService
import org.n1.av2.backend.entity.service.TimerType
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.other.TripwireLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.ServerActions.SERVER_TERMINAL_RECEIVE
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.run.TERMINAL_MAIN
import org.n1.av2.backend.service.site.SiteResetService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.site.toDuration
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.service.util.StompService.TerminalReceive
import org.n1.av2.backend.service.util.TimeService
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
    private val siteResetService: SiteResetService,
) {


    fun hack(layer: TripwireLayer) {
        if (layer.coreLayerId == null) {
            stompService.replyTerminalReceive("This tripwire is irreversible.")
            return
        }
        val node = nodeEntityService.findByLayerId(layer.coreLayerId!!)
        stompService.replyTerminalReceive("This tripwire is managed by core in node [ok]${node.networkId}")
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

        val identifiers = TaskIdentifiers(null, siteId, layer.id)
        systemTaskRunner.queueInSeconds("tripwire effect", identifiers, duration.seconds) { timerActivates(siteId, timer.id, layer) }
    }

    @ScheduledTask
    @CalledBySystem
    fun timerActivates(siteId: String, timerId: String, layer: TripwireLayer) {
        stompService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to timerId)
        timerEntityService.deleteById(timerId)

        val hackerStates = hackerStateEntityService.findAllHackersInSite(siteId)
        siteResetService.resetSite(siteId)
        stompService.toSite( siteId, SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, arrayOf("[info]Site down for maintenance for ${layer.shutdown}")))
        hackerStates
            .filter { it.activity == HackerActivity.INSIDE }
            .forEach { hackerState ->
                runService.hackerDisconnect(hackerState, "Disconnected (server abort)")
            }

        val shutdownDuration = layer.shutdown.toDuration("shutdown")
        val shutdownEndTime = time.now().plus(shutdownDuration)

        val shutdownTimer = timerEntityService.create(layer.id, null, shutdownEndTime, siteId, siteId, TimerType.SHUTDOWN_FINISH)
        stompService.toSite(siteId, ServerActions.SERVER_START_TIMER, toTimerInfo(shutdownTimer, layer))

        siteResetService.shutdownSite(siteId, shutdownEndTime)
        val identifiers = TaskIdentifiers(null, siteId, null)
        systemTaskRunner.queueInSeconds("site shutdown end", identifiers, shutdownDuration.seconds) { shutdownFinished(siteId, shutdownTimer.id) }
    }

    @ScheduledTask
    @CalledBySystem
    fun shutdownFinished(siteId: String, shutdownTimerId: String) {
        siteResetService.shutdownFinished(siteId)
        timerEntityService.deleteById(shutdownTimerId)
        stompService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to shutdownTimerId)
        stompService.toSite(siteId, SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, arrayOf("[info]Site connection available again.")))
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
            TimerType.SHUTDOWN_START -> "shutdown for ${shutdownTime}"
            TimerType.SHUTDOWN_FINISH -> "site available"
        }
    }


}