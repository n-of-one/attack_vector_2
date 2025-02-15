package org.n1.av2.platform.util

import org.n1.av2.editor.SiteValidationException
import org.n1.av2.platform.config.StaticConfig
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.LinkedList

@Service
class TimeService(
    staticConfig: StaticConfig,
    ) {
    val timeZoneId: ZoneId = if (staticConfig.timeZoneInput == "default") ZoneId.systemDefault() else ZoneId.of(staticConfig.timeZoneInput)

    private val dateTimeFOrmat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")


    fun now(): ZonedDateTime {
        return ZonedDateTime.now(timeZoneId)
    }

    fun longAgo(): ZonedDateTime {
        return ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, timeZoneId)
    }

    fun scriptResetMoment() = now()
            .withHour(4)
            .withMinute(0)
            .withSecond(0)

    fun formatDuration(duration: Duration): String {
        return String.format("%d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart())
    }

    fun formatDateTime(zonedDateTime: ZonedDateTime): String {
        return zonedDateTime.format(dateTimeFOrmat)
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

    if (hours < 0 || minutes < 0 || seconds <0) {
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
