package org.n1.av2.platform.util

import org.n1.av2.platform.config.StaticConfig
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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


    fun formatDuration(duration: Duration): String {
        return String.format("%d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart())
    }

    fun formatDateTime(zonedDateTime: ZonedDateTime): String {
        return zonedDateTime.format(dateTimeFOrmat)
    }

}
