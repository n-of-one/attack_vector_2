package org.n1.av2.layer.ice.tangle

import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.util.createId
import org.n1.av2.run.RunService
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.statistics.IceHackState
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Suppress("unused")
private class TangleLineSegment(val x1: Int, val y1: Int, val x2: Int, val y2: Int, val id: String)

@Service
class TangleService(
    private val tangleIceStatusRepo: TangleIceStatusRepo,
    private val connectionService: ConnectionService,
    private val hackedUtil: HackedUtil,
    private val runService: RunService,
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
        val clustersRevealed: Boolean,
        val quickPlaying: Boolean,
    )

    fun findOrCreateIceByLayerId(layer: TangleIceLayer): TangleIceStatus {
        return tangleIceStatusRepo.findByLayerId(layer.id) ?: createTangleIce(layer)

    }

    fun createTangleIce(layer: TangleIceLayer): TangleIceStatus {
        val creation = TangleCreator().create(layer.strength)

        val id = createId("tangle", tangleIceStatusRepo::findById)
        val iceTangleStatus = TangleIceStatus(
            id = id,
            layerId = layer.id,
            strength = layer.strength,
            originalPoints = creation.points,
            points = creation.points,
            lines = creation.lines,
            clustersRevealed = layer.strength == IceStrength.WEAK,
        )
        tangleIceStatusRepo.save(iceTangleStatus)
        return iceTangleStatus
    }


    fun enter(iceId: String) {
        val tangleStatus = tangleIceStatusRepo.findById(iceId).getOrElse { error("No Tangle ice for ID: ${iceId}") }
        val quickPlaying = configService.getAsBoolean(ConfigItem.DEV_QUICK_PLAYING)
        val clusters = TangleCreator.clusterCount(tangleStatus.strength)
        val uiState = UiTangleState(tangleStatus.strength, tangleStatus.points, tangleStatus.lines, clusters, tangleStatus.clustersRevealed, quickPlaying)
        connectionService.reply(ServerActions.SERVER_TANGLE_ENTER, uiState)
        runService.enterIce(iceId)
    }
    // Puzzle solving //

    data class TanglePointMoved(val id: String, val x: Int, val y: Int)

    fun move(command: TangleIceController.TanglePointMoveInput) {
        val x = keepInPlayArea(command.x, X_SIZE)
        val y = keepInPlayArea(command.y, Y_SIZE)

        val tangleStatus = tangleIceStatusRepo.findById(command.iceId).getOrElse { error("Ice not found for \"${command.iceId}\"") }

        val oldPoint = tangleStatus.points.find { it.id == command.pointId } ?: error("Point not found for id: \"${command.pointId}\"")
        val newPoint = oldPoint.copy(x = x, y =  y)

        tangleStatus.points.remove(oldPoint)
        tangleStatus.points.add(newPoint)
        tangleIceStatusRepo.save(tangleStatus)

        val message = TanglePointMoved(command.pointId, x, y)
        connectionService.toIce(command.iceId, ServerActions.SERVER_TANGLE_POINT_MOVED, message)

        val solved = tangleSolved(tangleStatus)
        if (solved) {
            hackedUtil.iceHacked(command.iceId, tangleStatus.layerId, 70, IceHackState.HACKED)
        }
    }

    private fun keepInPlayArea(position: Int, size: Int): Int {
        if (position > size - PADDING) return size - PADDING
        if (position < PADDING) return PADDING
        return position
    }


    private fun tangleSolved(tangleStatus: TangleIceStatus): Boolean {
        val segments = toSegments(tangleStatus)

        val uncheckedSegments = segments.toMutableList()
        while (!uncheckedSegments.isEmpty()) {
            val segment_1 = uncheckedSegments.removeAt(0)

            uncheckedSegments.forEach { segment_2 ->
                if (!connected(segment_1, segment_2)) {
                    val intersect = segmentsIntersect(
                        segment_1.x1, segment_1.y1, segment_1.x2, segment_1.y2,
                        segment_2.x1, segment_2.y1, segment_2.x2, segment_2.y2
                    )

                    if (intersect) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun toSegments(tangleStatus: TangleIceStatus): List<TangleLineSegment> {
        return tangleStatus.lines.map { line ->
            val from = tangleStatus.points.find { it.id == line.fromId }!!
            val to = tangleStatus.points.find { it.id == line.toId }!!

            TangleLineSegment(from.x, from.y, to.x, to.y, line.id)
        }
    }


    private fun connected(segment_1: TangleLineSegment, segment_2: TangleLineSegment): Boolean {
        return connected(segment_1.x1, segment_1.y1, segment_2) || connected(segment_1.x2, segment_1.y2, segment_2)
    }

    private fun connected(x: Int, y: Int, segment: TangleLineSegment): Boolean {
        return (x == segment.x1 && y == segment.y1) ||
                (x == segment.x2 && y == segment.y2)
    }

    fun resetIceByLayerId(layerId: String) {
        val iceStatus = tangleIceStatusRepo.findByLayerId(layerId) ?: return
        tangleIceStatusRepo.delete(iceStatus)

        connectionService.toIce(iceStatus.id, ServerActions.SERVER_RESET_ICE)
    }

    fun revealClusters(status: TangleIceStatus) {
        val updatedStatus = status.copy (
            clustersRevealed = true
        )
        tangleIceStatusRepo.save(updatedStatus)
        connectionService.toIce(status.id, ServerActions.SERVER_TANGLE_CLUSTERS_REVEALED)
    }

}
