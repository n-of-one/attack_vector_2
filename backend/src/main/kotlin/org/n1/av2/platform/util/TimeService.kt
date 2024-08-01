package org.n1.av2.platform.util

import org.n1.av2.platform.config.ServerConfig
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class TimeService(
    val config: ServerConfig
) {

    private val dateTimeFOrmat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")


    fun now(): ZonedDateTime {
        return ZonedDateTime.now(config.timeZoneId)
    }

    fun longAgo(): ZonedDateTime {
        return ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, config.timeZoneId)
    }


    fun formatDuration(duration: Duration): String {
        return String.format("%d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart())
    }

    fun formatDateTime(zonedDateTime: ZonedDateTime): String {
        return zonedDateTime.format(dateTimeFOrmat)
    }

}
