package org.n1.av2.backend.model.db.run

import org.springframework.data.annotation.Id
import java.time.ZonedDateTime

data class Run (
        @Id val id: String,
        val siteId: String,
        var startTime: ZonedDateTime,
        var endTime: ZonedDateTime

)