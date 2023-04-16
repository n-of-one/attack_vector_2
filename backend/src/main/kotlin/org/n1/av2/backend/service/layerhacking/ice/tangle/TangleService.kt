package org.n1.av2.backend.service.layerhacking.ice.tangle

import org.n1.av2.backend.entity.ice.TangleIceStatus
import org.n1.av2.backend.entity.ice.TangleIceStatusRepo
import org.n1.av2.backend.entity.ice.TangleLine
import org.n1.av2.backend.entity.ice.TanglePoint
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.layer.IceTangleLayer
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.HackedUtil
import org.n1.av2.backend.util.createId
import org.n1.av2.backend.web.ws.ice.IceTangleController
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse
import kotlin.system.measureNanoTime

private class TangleLineSegment(val x1: Int, val y1: Int, val x2: Int, val y2: Int, val id: String)

@Service
class TangleService(
    val tangleIceStatusRepo: TangleIceStatusRepo,
    val nodeEntityService: NodeEntityService,
    val stompService: StompService,
    val hackedUtil: HackedUtil,
) {

    companion object {
        const val X_SIZE = 1200
        const val Y_SIZE = 680
        const val PADDING = 10
    }


    data class UiTangleState(
        val strength: IceStrength,
        val points: MutableList<TanglePoint>,
        val lines: List<TangleLine>
    )

    private val logger = mu.KotlinLogging.logger {}


    fun enter(iceId: String) {
        val iceStatus = tangleIceStatusRepo
            .findById(iceId)
            .getOrElse {
                stompService.replyMessage(NotyMessage(NotyType.FATAL, "Error", "No ice for ID: ${iceId}"))
                return
            }

        val uiState = UiTangleState(iceStatus.strength, iceStatus.points, iceStatus.lines)
        stompService.reply(ServerActions.SERVER_ENTER_ICE_TANGLE, uiState)
    }


    fun createTangleIce(layer: IceTangleLayer, nodeId: String, runId: String): TangleIceStatus {
        val creation = TangleCreator().create(layer.strength)

        val id = createId("tangle", tangleIceStatusRepo::findById)
        val iceTangleStatus = TangleIceStatus(
            id = id,
            runId = runId,
            nodeId = nodeId,
            layerId = layer.id,
            strength = layer.strength,
            originalPoints = creation.points,
            points = creation.points,
            lines = creation.lines
        )
        tangleIceStatusRepo.save(iceTangleStatus)
        return iceTangleStatus
    }

    // Puzzle solving //

    data class TanglePointMoved(val id: String, val x: Int, val y: Int, val solved: Boolean)

    fun move(command: IceTangleController.TanglePointMoveInput) {
        val x = keepInPlayArea(command.x, X_SIZE)
        val y = keepInPlayArea(command.y, Y_SIZE)

        val tangleStatus = tangleIceStatusRepo.findById(command.iceId).getOrElse { error("Ice not found for \"${command.iceId}\"") }

        tangleStatus.points.removeIf { it.id == command.pointId }
        val newPoint = TanglePoint(command.pointId, x, y)
        tangleStatus.points.add(newPoint)

        tangleIceStatusRepo.save(tangleStatus)

        val solved = tangleSolved(tangleStatus)

        val message = TanglePointMoved(command.pointId, x, y, solved)

        stompService.toIce(command.iceId, ServerActions.SERVER_TANGLE_POINT_MOVED, message)

        if (solved) {
            hackedUtil.iceHacked(tangleStatus.layerId, tangleStatus.runId, 70)
        }

    }

    private fun keepInPlayArea(position: Int, size: Int): Int {
        if (position > size - PADDING) return size - PADDING
        if (position < PADDING) return PADDING
        return position
    }


    private fun tangleSolved(tangleStatus: TangleIceStatus): Boolean {

        val segments = toSegments(tangleStatus)

        var solved: Boolean?
        val nanos = measureNanoTime {
            solved = tangleSolvedInternal(segments)
        }
        logger.debug("Measure intersections of ${tangleStatus.points.size} took ${nanos} nanos")

        return solved!!
    }

    private fun toSegments(tangleStatus: TangleIceStatus): List<TangleLineSegment> {
        return tangleStatus.lines.map { line ->
            val from = tangleStatus.points.find { it.id == line.fromId }!!
            val to = tangleStatus.points.find { it.id == line.toId }!!

            TangleLineSegment(from.x, from.y, to.x, to.y, line.id)
        }
    }

    private fun tangleSolvedInternal(segments: List<TangleLineSegment>): Boolean {
        val uncheckedSegments = segments.toMutableList()
        var solved = true
        while (!uncheckedSegments.isEmpty()) {
            val segment_1 = uncheckedSegments.removeAt(0)


            uncheckedSegments.forEach { segment_2 ->
                if (!connected(segment_1, segment_2)) {
                    val intersect = segmentsIntersect(
                        segment_1.x1, segment_1.y1, segment_1.x2, segment_1.y2,
                        segment_2.x1, segment_2.y1, segment_2.x2, segment_2.y2
                    )

                    if (intersect) {
                        solved = false
//                        logger.debug("Intersect: ${segment_1.id} x ${segment_2.id}")
                    }
                }
            }
        }
        return solved
    }

    private fun connected(segment_1: TangleLineSegment, segment_2: TangleLineSegment): Boolean {
        return connected(segment_1.x1, segment_1.y1, segment_2) || connected(segment_1.x2, segment_1.y2, segment_2)
    }

    private fun connected(x: Int, y: Int, segment: TangleLineSegment): Boolean {
        return (x == segment.x1 && y == segment.y1) ||
                (x == segment.x2 && y == segment.y2)
    }
}