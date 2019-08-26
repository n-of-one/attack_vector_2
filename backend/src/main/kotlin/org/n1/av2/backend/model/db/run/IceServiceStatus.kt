package org.n1.av2.backend.model.db.run

import org.springframework.data.annotation.Id
import java.time.ZonedDateTime



data class IcePasswordStatus(
        @Id val id: String,
        val serviceId: String,
        val runId: String,
        val attempts: MutableList<String>,
        var lockedUntil: ZonedDateTime
)