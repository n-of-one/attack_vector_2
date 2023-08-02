package org.n1.av2.backend.service

import org.n1.av2.backend.config.ServerConfig
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZonedDateTime

@Service
class TimeService(
    val config: ServerConfig
) {

    fun now(): ZonedDateTime {
        return ZonedDateTime.now(config.timeZoneId)
    }

    fun longAgo(): ZonedDateTime {
        return ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, config.timeZoneId)
    }


    fun formatDuration(duration: Duration): String {
        return String.format("%d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart())
    }

}