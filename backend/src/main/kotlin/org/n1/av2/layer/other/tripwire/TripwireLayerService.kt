package org.n1.av2.layer.other.tripwire

import org.n1.av2.editor.toDuration
import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ConnectionService.TerminalReceive
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.connection.ServerActions.SERVER_TERMINAL_RECEIVE
import org.n1.av2.platform.engine.CalledBySystem
import org.n1.av2.platform.engine.ScheduledTask
import org.n1.av2.platform.engine.SystemTaskRunner
import org.n1.av2.platform.engine.TaskIdentifiers
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.util.TimeService
import org.n1.av2.run.RunService
import org.n1.av2.run.terminal.TERMINAL_MAIN
import org.n1.av2.site.SiteResetService
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.context.annotation.Lazy
import java.time.Duration
import java.time.ZonedDateTime

class TimerInfo(val timerId: String, val finishAt: ZonedDateTime, val type: TimerType, val target: String, val effect: String)


@org.springframework.stereotype.Service
class TripwireLayerService(
    private val connectionService: ConnectionService,
    private val time: TimeService,
    private val systemTaskRunner: SystemTaskRunner,
    private val timerEntityService: TimerEntityService,
    private val nodeEntityService: NodeEntityService,
    @Lazy private val runService: RunService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val siteResetService: SiteResetService,
    private val hackerEntityService: HackerEntityService,
    private val currentUserService: CurrentUserService,
) {


    fun hack(layer: TripwireLayer) {
        if (layer.coreLayerId == null) {
            connectionService.replyTerminalReceive("This tripwire is irreversible.")
            return
        }
        val node = nodeEntityService.findByLayerId(layer.coreLayerId!!)
        connectionService.replyTerminalReceive("This tripwire is managed by core in node [ok]${node.networkId}")
    }

    fun hackerArrivesNode(siteId: String, layer: TripwireLayer, nodeId: String, runId: String) {
        val existingDetection = timerEntityService.findByLayer(layer.id)
        if (existingDetection != null) {
            return
        }

        val (tripwireDuration, stealthAdjustmentDuration) = determineTripwireCountdownDuration(layer)
        val countdown = tripwireDuration + stealthAdjustmentDuration
        val shutdownTime = time.now().plus(countdown)

        val timer = timerEntityService.create(layer.id, null, shutdownTime, siteId, siteId, TimerType.SHUTDOWN_START)

        connectionService.toSite(siteId, ServerActions.SERVER_START_TIMER, toTimerInfo(timer, layer))
        connectionService.replyTerminalReceive("[pri]${layer.level}[/] Tripwire triggered [warn]site reset[/] in ${toDurationString(countdown)}[/].")
        if (stealthAdjustmentDuration != null) {
            val message = if (stealthAdjustmentDuration.isPositive)
                "Stealth skill increased the duration by ${toDurationString(stealthAdjustmentDuration)}."
            else
                "Low stealth skill decreased the duration by ${toDurationString(stealthAdjustmentDuration.multipliedBy(-1))}}"
            connectionService.replyTerminalReceive(message)
        }

        connectionService.toRun(runId, ServerActions.SERVER_FLASH_PATROLLER, "nodeId" to nodeId)

        val identifiers = TaskIdentifiers(null, siteId, layer.id)
        systemTaskRunner.queueInSeconds("tripwire effect", identifiers, countdown.seconds) { timerActivates(siteId, timer.id, layer) }
    }

    fun determineTripwireCountdownDuration(layer: TripwireLayer): Pair<Duration, Duration?> {
        val tripwireDuration = layer.countdown.toDuration("tripwire ${layer.id}")
//        val hacker = hackerEntityService.findForUser(currentUserService.userEntity)
//        val stealthSkill = hacker.skillAsIntOrNull(HackerSkillType.STEALTH) ?: 0
//        if (stealthSkill == 0) {
        return Pair(tripwireDuration, null)
//        }
//        val stealthFactor = (stealthSkill.toDouble() / 100.0)
//        val tripwireMillis = tripwireDuration.toMillis()
//        val stealthMillis = (tripwireMillis * stealthFactor).toLong()
//        val stealthAdjustmentDuration = Duration.ofMillis(stealthMillis)
//
//        return Pair(tripwireDuration, stealthAdjustmentDuration)
    }

    @ScheduledTask
    @CalledBySystem
    fun timerActivates(siteId: String, timerId: String, layer: TripwireLayer) {
        connectionService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to timerId)
        timerEntityService.deleteById(timerId)

        val hackerStates = hackerStateEntityService.findAllHackersInSite(siteId)
        siteResetService.resetSite(siteId)
        connectionService.toSite(
            siteId,
            SERVER_TERMINAL_RECEIVE,
            TerminalReceive(TERMINAL_MAIN, arrayOf("[info]Site down for maintenance for ${layer.shutdown}"))
        )
        hackerStates
            .filter { it.activity == HackerActivity.INSIDE }
            .forEach { hackerState ->
                runService.hackerDisconnect(hackerState, "Disconnected (server abort)")
            }

        val shutdownDuration = layer.shutdown.toDuration("shutdown")
        val shutdownEndTime = time.now().plus(shutdownDuration)

        val shutdownTimer = timerEntityService.create(layer.id, null, shutdownEndTime, siteId, siteId, TimerType.SHUTDOWN_FINISH)
        connectionService.toSite(siteId, ServerActions.SERVER_START_TIMER, toTimerInfo(shutdownTimer, layer))

        siteResetService.shutdownSite(siteId, shutdownEndTime)
        val identifiers = TaskIdentifiers(null, siteId, null)
        systemTaskRunner.queueInSeconds("site shutdown end", identifiers, shutdownDuration.seconds) { shutdownFinished(siteId, shutdownTimer.id) }
    }

    @ScheduledTask
    @CalledBySystem
    fun shutdownFinished(siteId: String, shutdownTimerId: String) {
        siteResetService.shutdownFinished(siteId)
        timerEntityService.deleteById(shutdownTimerId)
        connectionService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to shutdownTimerId)
        connectionService.toSite(siteId, SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, arrayOf("[info]Site connection available again.")))
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
