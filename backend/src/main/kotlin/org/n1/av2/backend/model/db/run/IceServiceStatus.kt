package org.n1.av2.backend.model.db.run

import java.time.ZonedDateTime



data class IcePasswordStatus(
        val attempts: MutableList<String>,
        var lockedUntil: ZonedDateTime?,
        var displayHint: Boolean = false
): ServiceStatus()