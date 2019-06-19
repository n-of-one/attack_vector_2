package org.n1.mainframe.backend.model.run

import java.time.ZonedDateTime

data class Run (
        val id: String,
        val siteId: String,
        var startTime: ZonedDateTime,
        var endTime: ZonedDateTime
)