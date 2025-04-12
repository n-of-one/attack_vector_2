package org.n1.av2.timer

import mu.KotlinLogging
import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ConnectionService.TerminalReceive
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.connection.ServerActions.SERVER_TERMINAL_RECEIVE
import org.n1.av2.platform.engine.CalledBySystem
import org.n1.av2.platform.engine.ScheduledTask
import org.n1.av2.platform.engine.SystemTaskRunner
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.util.TimeService
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.run.terminal.TERMINAL_MAIN
import org.n1.av2.site.SiteResetService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZonedDateTime


@Suppress("unused")
class TimerInfo(val timerId: String, val finishAt: ZonedDateTime, val type: TimerEffect, val target: String, val effectDescription: String)

@Service
class TimerService(
    private val connectionService: ConnectionService,
    private val timerEntityService: TimerEntityService,
    private val siteResetService: SiteResetService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val timeService: TimeService,
    private val systemTaskRunner: SystemTaskRunner,
    private val hackerEntityService: HackerEntityService,
    private val currentUserService: CurrentUserService,
) {

    private val MINIMUM_TIMER_DURATION = Duration.ofSeconds(10)
    private val logger = KotlinLogging.logger {}

    @EventListener(ApplicationStartedEvent::class)
    fun recreateTimerAfterApplicationRestart() {
        val timers = timerEntityService.findAll()
        timers.forEach { timer ->
            when (timer.effect) {
                TimerEffect.SHUTDOWN_FINISH -> {
                    val durationLeft = Duration.between(timeService.now(), timer.finishAt)
                    if (durationLeft.isPositive) {
                        scheduleShutdownFinish(timer.targetSiteId, durationLeft, timer.finishAt)
                        logger.info("Recreated timer for shutdown-end for ${timer.siteId}, ending in ${durationLeft.toHumanTime()}")
                    } else {
                        shutdownFinished(timer.targetSiteId, timer.id)
                        logger.info("Removed timer for shutdown end of ${timer.siteId}")
                    }
                }

                TimerEffect.SHUTDOWN_START -> {
                    logger.info("Removed timer for shutdown-start for ${timer.siteId}")
                    // Do nothing here, these are just removed
                    // The players will trigger them again when crossing the tripwire, nothing better we can do for now.
                }
            }
        }

        timers.forEach { timer ->
            timerEntityService.deleteById(timer.id)
        }
    }


    fun removeTimersForSite(siteId: String) {
        val timersRemoved = timerEntityService.deleteBySiteId(siteId)
        timersRemoved.forEach { connectionService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to it.id) }
    }

    private class Durations(val effective: Duration, val stealthAdjustment: Duration, val alertnessAdjustment: Duration)

    fun startShutdownTimer(
        siteId: String, layer: TripwireLayer?, baseTimerDuration: Duration, stealthApplies: Boolean,
        shutdownDuration: Duration, timerSourceMessage: String, timerLabel: TimerLabel?
    ) {
        val durations = calculateEffectiveDuration(baseTimerDuration, stealthApplies, siteId)
        val shutdownAt = timeService.now().plus(durations.effective)

        val timer = timerEntityService.create(layer?.id, null, shutdownAt, siteId, siteId, TimerEffect.SHUTDOWN_START, shutdownDuration, timerLabel)

        connectionService.toSite(siteId, ServerActions.SERVER_START_TIMER, toTimerInfo(timer))
        val identifiers = createTimerIdentifiers(siteId, layer?.id)
        systemTaskRunner.queueInSeconds("shutdown start timer", identifiers, durations.effective.seconds) {
            shutdownStart(
                siteId,
                timer.id,
                shutdownDuration
            )
        }

        connectionService.replyTerminalReceive("${timerSourceMessage} triggered [warn]site reset[/] in ${durations.effective.toHumanTime()}[/].")
        reportAdjustment("Stealth skill", "Low stealth skill", durations.stealthAdjustment)
        reportAdjustment("Decreased site alertness", "Increased site alertness", durations.alertnessAdjustment)
    }

    private fun reportAdjustment(sourcePostive: String, sourceNegative: String, adjustment: Duration) {
        if (adjustment != Duration.ZERO) {
            val message = if (adjustment.isPositive)
                "${sourcePostive} increased the duration by ${adjustment.toHumanTime()}."
            else
                "${sourceNegative} decreased the duration by ${adjustment.multipliedBy(-1).toHumanTime()}."
            connectionService.replyTerminalReceive(message)
        }
    }

    private fun calculateEffectiveDuration(originalDuration: Duration, stealthApplies: Boolean, siteId: String): Durations {
        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        val alertnessAdjustment = siteProperties.alertnessTimerAdjustment ?: Duration.ZERO

        val intermediateDuration = originalDuration + alertnessAdjustment
        val stealthAdjustment = calculateStealthAdjustment(stealthApplies, intermediateDuration)

        val effectiveDuration = intermediateDuration + stealthAdjustment

        // Always give the hackers a little bit of time, basically to increase the period of dread when the realize they messed up.
        val cappedDuration = if (effectiveDuration < MINIMUM_TIMER_DURATION) MINIMUM_TIMER_DURATION else effectiveDuration


        return Durations(cappedDuration, stealthAdjustment, alertnessAdjustment)
    }

    private fun calculateStealthAdjustment(stealthApplies: Boolean, effectiveDuration: Duration): Duration {
        if (!stealthApplies) return Duration.ZERO


        val hacker = hackerEntityService.findForUser(currentUserService.userEntity)
        val stealthSkill = hacker.skillAsIntOrNull(SkillType.STEALTH) ?: 0
        if (stealthSkill == 0) {
            return Duration.ZERO
        }
        val stealthFactor = (stealthSkill.toDouble() / 100.0)
        val tripwireMillis = effectiveDuration.toMillis()
        val stealthMillis = (tripwireMillis * stealthFactor).toLong()
        return Duration.ofMillis(stealthMillis)
    }


    @ScheduledTask
    @CalledBySystem
    fun shutdownStart(siteId: String, timerId: String, shutdownDuration: Duration) {
        connectionService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to timerId)
        timerEntityService.deleteById(timerId)

        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        if (siteProperties.shutdownEnd != null) {
            return // site already shut down because of another timer
        }

        siteResetService.resetSite(siteId, shutdownDuration)

        val shutdownEndTime = timeService.now().plus(shutdownDuration)
        val shutdownFinishTimer = scheduleShutdownFinish(siteId, shutdownDuration, shutdownEndTime)

        connectionService.toSite(siteId, ServerActions.SERVER_START_TIMER, toTimerInfo(shutdownFinishTimer))
        siteResetService.shutdownSite(siteId, shutdownEndTime)
    }

    private fun scheduleShutdownFinish(siteId: String, shutdownDuration: Duration, shutdownEndTime: ZonedDateTime): Timer {
        val shutdownFinishTimer = timerEntityService.create(null, null, shutdownEndTime, siteId, siteId, TimerEffect.SHUTDOWN_FINISH, Duration.ZERO)
        val identifiers = mapOf("siteId" to siteId)
        systemTaskRunner.queueInSeconds("site shutdown end", identifiers, shutdownDuration.seconds) {
            shutdownFinished(siteId, shutdownFinishTimer.id)
        }
        return shutdownFinishTimer
    }

    @ScheduledTask
    @CalledBySystem
    fun shutdownFinished(siteId: String, shutdownTimerId: String) {
        siteResetService.shutdownFinished(siteId)
        timerEntityService.deleteById(shutdownTimerId)
        connectionService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to shutdownTimerId)
        connectionService.toSite(siteId, SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, arrayOf("[info]Site connection available again.")))
    }

    fun findForEnterSite(siteId: String, userId: String): List<TimerInfo> {
        val timers = timerEntityService.findForEnterSite(siteId, userId)

        val countdowns: List<TimerInfo> = timers.map { timer ->
            toTimerInfo(timer)
        }

        return countdowns
    }

    fun toTimerInfo(timer: Timer): TimerInfo {
        return TimerInfo(timer.id, timer.finishAt, timer.effect, "site", determineEffect(timer.effect, timer.effectDuration))
    }

    private fun determineEffect(type: TimerEffect, duration: Duration): String {
        return when (type) {
            TimerEffect.SHUTDOWN_START -> "shutdown for ${duration.toHumanTime()}"
            TimerEffect.SHUTDOWN_FINISH -> "site available"
        }
    }

    fun delayTripwireTimer(layer: TripwireLayer, duration: Duration, siteId: String) {
        val timer = timerEntityService.findByLayer(layer.id) ?: error("No active countdown timer found for tripwire-layer ${layer.id}")

        val updatedTimer = timer.copy(finishAt = timer.finishAt.plus(duration))
        timerEntityService.update(updatedTimer)

        connectionService.replyTerminalReceive("Tripwire countdown delayed by ${duration.toHumanTime()}")
        connectionService.toSite(siteId, ServerActions.SERVER_CHANGE_TIMER, toTimerInfo(updatedTimer))

        alterTimerTask(updatedTimer, layer, duration, siteId)
    }

    fun alterTimerTask(timer: Timer, layer: TripwireLayer?, delta: Duration, siteId: String) {
        val taskIdentifiers = createTimerIdentifiers(siteId, layer?.id)
        val taskInfo = systemTaskRunner.removeTask(taskIdentifiers)

        val newCountdownSeconds = taskInfo.inSeconds + delta.toSeconds() + 1 // +1 to compensate for the time it takes to queue the task
        systemTaskRunner.queueInSeconds(taskInfo.description, taskIdentifiers, newCountdownSeconds) { shutdownStart(siteId, timer.id, timer.effectDuration) }
    }

    fun speedUpScriptResetTimer(duration: Duration, siteId: String) {
        val timers = timerEntityService.findByTargetSiteId(siteId).filter { it.label == TimerLabel.SCRIPT_SITE_SHUTDOWN }
        if (timers.isEmpty()) {
            return // no timers yet.
        }
        if (timers.size > 1) {
            connectionService.replyTerminalReceive("The script encountered an error: multiple script shutdown timers found.")
            return
        }

        val timer = timers.first()
        val adjustedFinishAt = timer.finishAt.minus(duration)
        if (adjustedFinishAt.isBefore(timeService.now())) {
            systemTaskRunner.queueInSeconds("system shutdown", mapOf("siteId" to siteId), 1) {
                shutdownStart(siteId, timer.id, timer.effectDuration)
            }
            return
        }

        val updatedTimer = timer.copy(finishAt = adjustedFinishAt)
        timerEntityService.update(updatedTimer)

        connectionService.replyTerminalReceive("System countdown sped up by ${duration.toHumanTime()}")
        connectionService.toSite(siteId, ServerActions.SERVER_CHANGE_TIMER, toTimerInfo(updatedTimer))

        alterTimerTask(updatedTimer, null, duration.negated(), siteId)
    }

    private fun createTimerIdentifiers(siteId: String, layerId: String?): Map<String, String> {
        if (layerId != null) {
            return mapOf("siteId" to siteId, "layerId" to layerId)
        }
        return mapOf("siteId" to siteId)
    }

}
