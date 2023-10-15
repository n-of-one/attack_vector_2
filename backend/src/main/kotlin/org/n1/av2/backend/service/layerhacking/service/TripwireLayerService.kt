package org.n1.av2.backend.service.layerhacking.service

import org.n1.av2.backend.engine.ScheduledTask
import org.n1.av2.backend.engine.SystemTaskRunner
import org.n1.av2.backend.entity.run.HackerActivity
import org.n1.av2.backend.entity.run.HackerStateEntityService
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
import org.springframework.context.annotation.Lazy
import java.time.Duration
import java.time.ZonedDateTime

class TimerInfo(val timerId: String, val finishAt: ZonedDateTime, val type: String, val target: String, val effect: String)


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
        val alarmTime = time.now().plus(duration)

        val timer = timerEntityService.create(layer.id, null, alarmTime, siteId, siteId, TimerType.SITE_RESET)
        val effect = determineEffect(layer.shutdown)

        stompService.reply(ServerActions.SERVER_START_TIMER, TimerInfo(timer.id, alarmTime, "tripwire", "site", effect))

        stompService.replyTerminalReceive("[pri]${layer.level}[/] Tripwire triggered [error]site reset[/] in ${toDurationString(duration)}[/].")

        class FlashPatroller(val nodeId: String)
        stompService.toRun(runId, ServerActions.SERVER_FLASH_PATROLLER, FlashPatroller(nodeId))

        systemTaskRunner.queueInSeconds(siteId, duration.seconds) { timerActivates(siteId, timer.id) }
    }

    @ScheduledTask
    fun timerActivates(siteId: String, timerId: String) {
        stompService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to timerId)

        val hackerStates = hackerStateEntityService.findAllHackersInSite(siteId)
        siteService.resetSite(siteId)
        hackerStates
            .filter { it.activity == HackerActivity.ATTACKING }
            .forEach { hackerState ->
                runService.hackerDisconnect(hackerState, "Disconnected (server abort)")
            }

        // FIXME: also possibly shut down
    }

    private fun determineEffect(shutdownTime: String): String {
        val shutdownDuration = shutdownTime.toDuration("shutdown")
        if (shutdownDuration.isZero) {
            return "site reset"
        }
        return "shutdown (${toDurationString(shutdownDuration)})"
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
            TimerInfo(timer.id, timer.finishAt, "tripwire", "site", determineEffect(layer.shutdown))
        }

        return countdowns
    }
}