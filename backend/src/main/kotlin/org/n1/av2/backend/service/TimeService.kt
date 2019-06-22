package org.n1.av2.backend.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class TimeService(@Value("#{environment.TIME_ZONE}") timeZoneInput: String = ZoneId.systemDefault().id) {


    val zoneId = ZoneId.of(timeZoneInput)

    fun now(): ZonedDateTime {
        return ZonedDateTime.now(zoneId)
    }

    fun formatDuration(duration: Duration): String {
        return String.format("%d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart())
    }

}