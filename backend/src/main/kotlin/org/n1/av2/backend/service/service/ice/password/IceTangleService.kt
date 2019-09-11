package org.n1.av2.backend.service.service.ice.password

import mu.KLogging
import org.n1.av2.backend.model.db.layer.IceTangleLayer
import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.run.IceTangleStatus
import org.n1.av2.backend.model.db.run.TangleLine
import org.n1.av2.backend.model.db.run.TanglePoint
import org.n1.av2.backend.model.db.site.enums.IceStrength
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.IceStatusRepo
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.service.HackedUtil
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.util.createId
import org.n1.av2.backend.util.nodeIdFromServiceId
import org.n1.av2.backend.web.ws.ice.IceTangleController
import org.springframework.stereotype.Service
import kotlin.system.measureNanoTime

const val X_SIZE = 1200
const val Y_SIZE = 680
const val PADDING = 10

private class TangleLineSegment(val x1: Int, val y1: Int, val x2: Int, val y2: Int, val id: String)

@Service
class IceTangleService(
        val iceStatusRepo: IceStatusRepo,
        val nodeService: NodeService,
        val hackedUtil: HackedUtil,
        val stompService: StompService) {

    data class UiTangleState(
            val layerId: String,
            val strength: IceStrength,
            val points: MutableList<TanglePoint>,
            val lines: List<TangleLine>
    )

    companion object : KLogging()

    fun hack(layer: IceTangleLayer, runId: String) {
        val tangleStatus = getOrCreateStatus(layer, runId)
        val uiState = UiTangleState(layer.id, layer.strength, tangleStatus.points, tangleStatus.lines)
        stompService.toUser(ReduxActions.SERVER_START_HACKING_ICE_TANGLE, uiState)
    }

    private fun getOrCreateStatus(layer: Layer, runId: String): IceTangleStatus {
        return getStatus(layer.id, runId) ?: createServiceStatus(layer, runId)
    }

    private fun getStatus(layerId: String, runId: String): IceTangleStatus? {
        val tangleStatus = iceStatusRepo.findByLayerIdAndRunId(layerId, runId) ?: return null
        return tangleStatus as IceTangleStatus
    }

    private fun createServiceStatus(layer: Layer, runId: String): IceTangleStatus {
        val nodeId = nodeIdFromServiceId(layer.id)
        val node = nodeService.getById(nodeId)

        val iceConfig = node.layers.find {it.id == layer.id }!! as IceTangleLayer


        val creation = TangleCreator().create(iceConfig.strength)

        val id = createId("iceTangleStatus-")
        val iceTangleStatus = IceTangleStatus(id, layer.id, runId, creation.points, creation.points, creation.lines)
        iceStatusRepo.save(iceTangleStatus)
        return iceTangleStatus
    }

    // Puzzle solving //

    data class TanglePointMoved(val layerId: String, val id: String, val x: Int, val y: Int, val solved: Boolean)

    fun move(command: IceTangleController.TanglePointMoveInput) {
        val x = keepInPlayArea(command.x, X_SIZE)
        val y = keepInPlayArea(command.y, Y_SIZE)

        val tangleStatus = getStatus(command.layerId, command.runId)!!

        tangleStatus.points.removeIf { it.id == command.id }
        val newPoint = TanglePoint(command.id, x, y)
        tangleStatus.points.add(newPoint)

        iceStatusRepo.save(tangleStatus)

        val solved = tangleSolved(tangleStatus)

        val message = TanglePointMoved(command.layerId, command.id, x, y, solved)

        stompService.toRun(command.runId, ReduxActions.SERVER_TANGLE_POINT_MOVED, message)

        if (solved) {
            hackedUtil.iceHacked(command.layerId, command.runId, 70)
        }

    }

    private fun keepInPlayArea(position: Int, size: Int): Int {
        if (position > size - PADDING) return size - PADDING
        if (position < PADDING) return PADDING
        return position
    }


    private fun tangleSolved(tangleStatus: IceTangleStatus): Boolean {

        val segments = toSegments(tangleStatus)

        var solved: Boolean? = null
        val nanos = measureNanoTime {
            solved = tangleSolvedInternal(segments)
        }
        logger.debug("Measure intersections of ${tangleStatus.points.size} took ${nanos} nanos")

        return solved!!
    }

    private fun toSegments(tangleStatus: IceTangleStatus): List<TangleLineSegment> {
        return tangleStatus.lines.map { line ->
            val from = tangleStatus.points.find {it.id ==line.fromId }!!
            val to = tangleStatus.points.find {it.id == line.toId }!!

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
                            segment_2.x1, segment_2.y1, segment_2.x2, segment_2.y2)

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