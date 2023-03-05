package org.n1.av2.backend.entity.run

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document
open class IceStatus(
        @Id val id: String,
        val layerId: String,
        val runId: String
)

class IcePasswordStatus(
        id: String,
        layerId: String,
        runId: String,
        val attempts: MutableList<String>,
        var lockedUntil: ZonedDateTime
) : IceStatus(id, layerId, runId)

