package org.n1.av2.backend.model.db.run

import java.time.ZonedDateTime

data class Run (
        val id: String,
        val siteId: String,
        var startTime: ZonedDateTime,
        var endTime: ZonedDateTime
)