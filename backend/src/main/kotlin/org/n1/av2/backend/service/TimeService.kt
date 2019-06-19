package org.n1.av2.backend.service

import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class TimeService {

    val timeZone = ZoneId.of("Europe/Amsterdam")

    fun now(): ZonedDateTime {
        return ZonedDateTime.now(timeZone)
    }

}