package org.n1.mainframe.backend.service

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