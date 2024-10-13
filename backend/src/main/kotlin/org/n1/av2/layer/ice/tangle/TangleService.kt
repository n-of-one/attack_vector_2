package org.n1.av2.layer.ice.tangle

import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.util.createId
import org.n1.av2.run.RunService
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.enums.IceStrength
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse
import kotlin.system.measureNanoTime

private class TangleLineSegment(val x1: Int, val y1: Int, val x2: Int, val y2: Int, val id: String)

@Service
class TangleService(
    private val tangleIceStatusRepo: TangleIceStatusRepo,
    private val connectionService: ConnectionService,
    private val hackedUtil: HackedUtil,
    private val runService: RunService,
    private val nodeEntityService: NodeEntityService,
    private val configService: ConfigService,
) {

    companion object {
        const val X_SIZE = 1576
        const val Y_SIZE = 828
        const val PADDING = 10
    }


    data class UiTangleState(
        val strength: IceStrength,
        val points: MutableList<TanglePoint>,
        val lines: List<TangleLine>,
        val clusters: Int,
        val hacked: Boolean,
        val quickPlaying: Boolean,
    )

    private val logger = mu.KotlinLogging.logger {}

    fun findOrCreateIceByLayerId(layer: TangleIceLayer): TangleIceStatus {
        return tangleIceStatusRepo.findByLayerId(layer.id) ?: createTangleIce(layer)

    }

    fun createTangleIce(layer: TangleIceLayer): TangleIceStatus {
        val creation = TangleCreator().create(layer.strength, layer.clusters)

        val id = createId("tangle", tangleIceStatusRepo::findById)
        val iceTangleStatus = TangleIceStatus(
            id = id,
            layerId = layer.id,
            strength = layer.strength,
            originalPoints = creation.points,
            points = creation.points,
            lines = creation.lines
        )
        tangleIceStatusRepo.save(iceTangleStatus)
        return iceTangleStatus
    }


    fun enter(iceId: String) {
        val tangleStatus = tangleIceStatusRepo.findById(iceId).getOrElse { error("No Tangle ice for ID: ${iceId}") }
        val layer = nodeEntityService.findLayer(tangleStatus.layerId) as TangleIceLayer
        val quickPlaying = configService.getAsBoolean(ConfigItem.DEV_QUICK_PLAYING)
        val uiState = UiTangleState(tangleStatus.strength, tangleStatus.points, tangleStatus.lines, layer.clusters ?: 1, layer.hacked, quickPlaying)
        connectionService.reply(ServerActions.SERVER_TANGLE_ENTER, uiState)
        runService.enterNetworkedApp(iceId)
    }
    // Puzzle solving //

    data class TanglePointMoved(val id: String, val x: Int, val y: Int, val solved: Boolean)

    fun move(command: TangleIceController.TanglePointMoveInput) {
        val x = keepInPlayArea(command.x, X_SIZE)
        val y = keepInPlayArea(command.y, Y_SIZE)

        val tangleStatus = tangleIceStatusRepo.findById(command.iceId).getOrElse { error("Ice not found for \"${command.iceId}\"") }

        tangleStatus.points.removeIf { it.id == command.pointId }
        val newPoint = TanglePoint(command.pointId, x, y)
        tangleStatus.points.add(newPoint)
        tangleIceStatusRepo.save(tangleStatus)

        val solved = tangleSolved(tangleStatus)
        if (solved) {
            hackedUtil.iceHacked(tangleStatus.layerId, 70)
        }

        val message = TanglePointMoved(command.pointId, x, y, solved)
        connectionService.toIce(command.iceId, ServerActions.SERVER_TANGLE_POINT_MOVED, message)


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

    fun deleteByLayerId(layerId: String) {
        val status = tangleIceStatusRepo.findByLayerId(layerId) ?: return
        tangleIceStatusRepo.delete(status)
    }

}
