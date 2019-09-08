package org.n1.av2.backend.model.db.run

import org.springframework.data.annotation.Id
import java.time.ZonedDateTime


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

class IceTangleStatus(
        id: String,
        layerId: String,
        runId: String,
        val originalPoints: MutableList<TanglePoint>,
        val points: MutableList<TanglePoint>,
        val lines: List<TangleLine>

) : IceStatus(id, layerId, runId)

data class TanglePoint(val id: Int, val x: Int, val y: Int)

enum class TangleLineType { NORMAL, SETUP}
data class TangleLine(val id: Int, val fromId: Int, val toId: Int, val type: TangleLineType)

