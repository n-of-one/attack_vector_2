package org.n1.av2.platform.util

import org.n1.av2.editor.SiteValidationException
import org.n1.av2.platform.config.StaticConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@Configuration
class clockConfiguration(val staticConfig: StaticConfig) {

    @Bean
    fun clock(): Clock {
        val timeZoneId: ZoneId = if (staticConfig.timeZoneInput == "default") ZoneId.systemDefault() else ZoneId.of(staticConfig.timeZoneInput)
        return Clock.system(timeZoneId)
    }
}

@Service
class TimeService(
    val clock: Clock
) {
    val longAgo: ZonedDateTime = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())

    private val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val resetTime = LocalTime.of(6, 0)


    fun now(): ZonedDateTime {
        return ZonedDateTime.now(clock)
    }
    fun scriptResetMomentToday(): ZonedDateTime = now()
        .withHour(resetTime.hour)
        .withMinute(resetTime.minute)
        .withSecond(resetTime.second)
        .withNano(resetTime.nano)

    /**
     * Is the moment the script has been last used, after the last reset moment?
     * A script that has been used today at 5:00 is "past reset" if now is today 10:00, because the reset moment was today at 6:00 today.
     * But it would not be "past reset" if now is today at 5:30, because reset will still be another 30 minutes.
     */
    fun isPastReset(lastUsed: ZonedDateTime?): Boolean {
        if (lastUsed == null) return true
        val todayResetMoment = scriptResetMomentToday()
        val resetMoment = if (now() > todayResetMoment) todayResetMoment else todayResetMoment.minusDays(1)

        return lastUsed < resetMoment
    }

    fun atResetMoment(localDate: LocalDate): ZonedDateTime {
        return ZonedDateTime.of(localDate.year, localDate.monthValue, localDate.dayOfMonth, 6, 0, 0, 0, clock.zone)
    }

    fun formatDuration(duration: Duration): String {
        return String.format("%d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart())
    }

    fun formatDateTime(zonedDateTime: ZonedDateTime): String {
        return zonedDateTime.format(dateTimeFormat)
    }

    /**
     * Before 6 the current payout date is yesterday, after 6 it is today.
     */
    fun currentPayoutDate(): LocalDate {
        return if (now().toLocalTime().isBefore(resetTime)) {
            now().toLocalDate().minusDays(1)
        } else {
            now().toLocalDate()
        }
    }

}


fun String.toDuration(): Duration {

    val errorText = this.validateDuration()
    if (errorText != null) throw SiteValidationException(errorText)

    if (this.trim() == "0") return Duration.ZERO

    val parts = this.split(":")
    val normalizedParts = if (parts.size == 3) parts else listOf("0") + parts
    val hours = normalizedParts[0].toLong()
    val minutes = normalizedParts[1].toLong()
    val seconds = normalizedParts[2].toLong()

    return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds)
}

const val duration_error = "duration must be \"0\" or in the format (hours):(minutes):(seconds) or (minutes):(second) for example: 0:12:00 or 12:00"
fun String.validateDuration(): String? {

    if (this.trim() == "0") return null // 0 is a valid duration

    val parts = this.split(":")

    if (parts.size < 2 || parts.size > 3) return duration_error

    val normalizedParts = if (parts.size == 3) parts else listOf("0") + parts

    val hours = normalizedParts[0].toLongOrNull() ?: return duration_error
    val minutes = normalizedParts[1].toLongOrNull() ?: return duration_error
    val seconds = normalizedParts[2].toLongOrNull() ?: return duration_error

    if (hours < 0 || minutes < 0 || seconds < 0) {
        return "duration must be positive."
    }

    return null
}

fun toHumanTime(durationString: String): String? {
    return durationString.toDuration().toHumanTime()
}

fun Duration.toHumanTime(): String {
    val textParts = LinkedList<String>()
    val hours = this.toHours()
    if (hours > 0) textParts.add("$hours hour${pluralS(hours)}")

    val minutes = this.toMinutesPart()
    if (minutes > 0) textParts.add("$minutes minute${pluralS(minutes)}")

    val seconds = this.toSecondsPart()
    if (seconds > 0) textParts.add("$seconds second${pluralS(seconds)}")

    if (textParts.isEmpty()) return "0 seconds"

    return textParts.joinToString(" ")
}
