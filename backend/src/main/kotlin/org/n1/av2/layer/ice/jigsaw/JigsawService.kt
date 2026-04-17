package org.n1.av2.layer.ice.jigsaw

import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.SECONDS_IN_TICKS
import org.n1.av2.platform.util.createId
import org.n1.av2.run.RunService
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.statistics.IceHackState
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private const val SNAP_TOLERANCE_PIXELS = 15.0

/**
 * Authoritative server for the Jigsaw ICE. See elegant-knitting-kahn plan doc for the full
 * protocol. Core rules:
 *
 *  - Client sends `moved` and `rotate`, both addressed by groupId.
 *  - After each command the server runs settleSnaps on the seed group: it finds all other groups
 *    whose pieces line up (grid-neighbour + matching rotation + within SNAP_TOLERANCE_PIXELS)
 *    and merges them transitively. Bridge snaps collapse multiple groups in one pass.
 *  - A single command produces exactly one broadcast: MOVED, ROTATE, or SNAP.
 *  - Puzzle solved when groups.size == 1 && rotation == 0.
 */
@Service
class JigsawService(
    private val jigsawIceStatusRepo: JigsawIceStatusRepo,
    private val connectionService: ConnectionService,
    private val hackedUtil: HackedUtil,
    private val runService: RunService,
    private val configService: ConfigService,
) {

    // --- Wire payloads ---------------------------------------------------

    @Suppress("unused")
    class JigsawEnter(
        val hacked: Boolean,
        val strength: IceStrength,
        val imageSource: String,
        val columns: Int,
        val rows: Int,
        val pieces: List<PieceConfig>,
        val groups: List<JigsawGroup>,
    )

    @Suppress("unused")
    class JigsawMovedMessage(val groupId: String, val x: Double, val y: Double)

    @Suppress("unused")
    class JigsawRotateMessage(val groupId: String, val rotation: Int)

    @Suppress("unused")
    class JigsawSnapMessage(
        val survivingGroupId: String,
        val absorbedGroupIds: List<String>,
        val pieces: List<GridPosition>,
        val rotation: Int,
        val x: Double,
        val y: Double,
    )

    // --- Lifecycle -------------------------------------------------------

    fun findOrCreateIceByLayerId(layer: JigsawIceLayer): JigsawIceStatus {
        return jigsawIceStatusRepo.findByLayerId(layer.id) ?: run {
            val iceId = createId("jigsaw", jigsawIceStatusRepo::findById)
            createIce(iceId, layer.id, layer.strength)
        }
    }

    fun createIce(iceId: String, layerId: String, strength: IceStrength): JigsawIceStatus {
        val testingMode = configService.getAsBoolean(ConfigItem.DEV_TESTING_MODE)
        val creation = JigsawCreator(testingMode).create(strength)

        val status = JigsawIceStatus(
            id = iceId,
            layerId = layerId,
            strength = strength,
            hacked = false,
            columns = creation.columns,
            rows = creation.rows,
            imageSource = creation.imageSource,
            pieces = creation.pieces,
            groups = creation.groups,
        )
        jigsawIceStatusRepo.save(status)
        return status
    }

    fun enter(iceId: String) {
        val status = jigsawIceStatusRepo.findById(iceId).getOrElse { error("No Jigsaw ice for ID: $iceId") }
        val payload = JigsawEnter(
            hacked = status.hacked,
            strength = status.strength,
            imageSource = status.imageSource,
            columns = status.columns,
            rows = status.rows,
            pieces = status.pieces,
            groups = status.groups,
        )
        connectionService.reply(ServerActions.SERVER_JIGSAW_ENTER, payload)
        runService.enterIce(iceId)
    }

    fun resetIceByLayerId(layerId: String) {
        val status = jigsawIceStatusRepo.findByLayerId(layerId) ?: return
        jigsawIceStatusRepo.delete(status)
        connectionService.toIce(status.id, ServerActions.SERVER_RESET_ICE)
    }

    // --- Gameplay --------------------------------------------------------

    fun moved(iceId: String, groupId: String, x: Double, y: Double) {
        val status = jigsawIceStatusRepo.findById(iceId).getOrElse { error("No Jigsaw ice for ID: $iceId") }
        if (status.hacked) return
        val group = status.groups.find { it.id == groupId } ?: return // already absorbed; drop silently

        val movedGroup = group.copy(x = x, y = y)
        val groupsAfterMove = status.groups.map { if (it.id == groupId) movedGroup else it }
        val afterMove = status.copy(groups = groupsAfterMove)

        val settled = settleSnaps(afterMove, movedGroup.id)
        if (settled == null) {
            jigsawIceStatusRepo.save(afterMove)
            connectionService.toIce(iceId, ServerActions.SERVER_JIGSAW_MOVED, JigsawMovedMessage(groupId, x, y))
            return
        }
        finishWithSnap(iceId, settled)
    }

    fun rotate(iceId: String, groupId: String, rotation: Int) {
        require(rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270) {
            "Invalid rotation: $rotation (must be 0/90/180/270)"
        }
        val status = jigsawIceStatusRepo.findById(iceId).getOrElse { error("No Jigsaw ice for ID: $iceId") }
        if (status.hacked) return
        val group = status.groups.find { it.id == groupId } ?: return // already absorbed; drop silently

        val rotatedGroup = group.copy(rotation = rotation)
        val groupsAfterRotate = status.groups.map { if (it.id == groupId) rotatedGroup else it }
        val afterRotate = status.copy(groups = groupsAfterRotate)

        val settled = settleSnaps(afterRotate, rotatedGroup.id)
        if (settled == null) {
            jigsawIceStatusRepo.save(afterRotate)
            connectionService.toIce(iceId, ServerActions.SERVER_JIGSAW_ROTATE, JigsawRotateMessage(groupId, rotation))
            return
        }
        finishWithSnap(iceId, settled)
    }

    private fun finishWithSnap(iceId: String, settled: SettleResult) {
        jigsawIceStatusRepo.save(settled.status)
        val surviving = settled.survivingGroup
        connectionService.toIce(
            iceId, ServerActions.SERVER_JIGSAW_SNAP,
            JigsawSnapMessage(
                survivingGroupId = surviving.id,
                absorbedGroupIds = settled.absorbedIds,
                pieces = surviving.pieces,
                rotation = surviving.rotation,
                x = surviving.x,
                y = surviving.y,
            )
        )
        checkSolved(iceId, settled.status)
    }

    private fun checkSolved(iceId: String, status: JigsawIceStatus) {
        if (status.hacked) return
        val groups = status.groups
        if (groups.size != 1 || groups[0].rotation != 0) return

        jigsawIceStatusRepo.save(status.copy(hacked = true))
        hackedUtil.iceHacked(iceId, status.layerId, 4 * SECONDS_IN_TICKS, IceHackState.HACKED)
    }

    // --- Snap engine -----------------------------------------------------

    private data class SettleResult(
        val status: JigsawIceStatus,
        val survivingGroup: JigsawGroup,
        val absorbedIds: List<String>,
    )

    private data class SnapCandidate(
        val otherGroup: JigsawGroup,
        val correctionX: Double,
        val correctionY: Double,
        val distance: Double,
    )

    /**
     * Runs the snap closure starting from the given seed group. Returns null if no merge happened;
     * otherwise returns the post-merge status, the final surviving group, and the ordered list of
     * absorbed group ids.
     */
    private fun settleSnaps(initialStatus: JigsawIceStatus, seedGroupId: String): SettleResult? {
        val pieceWidth = pieceWidth(initialStatus.columns)
        val pieceHeight = pieceHeight(initialStatus.rows)

        var status = initialStatus
        var seed = status.groups.find { it.id == seedGroupId }!!
        val absorbed = mutableListOf<String>()

        while (true) {
            val candidates = findSnapCandidates(status, seed, pieceWidth, pieceHeight)
            if (candidates.isEmpty()) break

            // Apply the best (closest) candidate's correction to the seed so the merged geometry
            // lands on exact grid positions; all other in-tolerance neighbours will follow along
            // because the final-solution geometry is internally rigid.
            val best = candidates.minBy { it.distance }
            val correctedSeed = seed.copy(
                x = seed.x + best.correctionX,
                y = seed.y + best.correctionY,
            )
            val mergePartners = candidates.map { it.otherGroup }

            val merged = mergeGroups(correctedSeed, mergePartners, pieceWidth, pieceHeight)

            val partnerIds = mergePartners.map { it.id }.toSet()
            val newGroups = status.groups
                .filter { it.id != correctedSeed.id && it.id !in partnerIds }
                .plus(merged)

            status = status.copy(groups = newGroups)
            absorbed += partnerIds.filter { it != merged.id }
            if (merged.id != correctedSeed.id) absorbed += correctedSeed.id
            seed = merged
        }

        if (absorbed.isEmpty()) return null
        return SettleResult(status, seed, absorbed.distinct())
    }

    private fun findSnapCandidates(
        status: JigsawIceStatus,
        seed: JigsawGroup,
        pieceWidth: Double,
        pieceHeight: Double,
    ): List<SnapCandidate> {
        val seedBodyCenters: Map<String, Pair<Double, Double>> = seed.pieces.associate {
            "${it.column}:${it.row}" to bodyCenter(seed, it, pieceWidth, pieceHeight)
        }

        val angleRad = Math.toRadians(seed.rotation.toDouble())
        val cos = cos(angleRad)
        val sin = sin(angleRad)

        data class BestPerGroup(val correctionX: Double, val correctionY: Double, val distance: Double)

        val bestByGroupId = mutableMapOf<String, BestPerGroup>()

        for (otherGroup in status.groups) {
            if (otherGroup.id == seed.id) continue
            if (otherGroup.rotation != seed.rotation) continue

            for (otherPiece in otherGroup.pieces) {
                // Which of the seed's pieces is a grid neighbour (4-connected) of otherPiece?
                val seedNeighbours = seed.pieces.filter { sp ->
                    val dc = otherPiece.column - sp.column
                    val dr = otherPiece.row - sp.row
                    (dc == 1 && dr == 0) || (dc == -1 && dr == 0) || (dc == 0 && dr == 1) || (dc == 0 && dr == -1)
                }
                if (seedNeighbours.isEmpty()) continue

                val otherWorldCenter = bodyCenter(otherGroup, otherPiece, pieceWidth, pieceHeight)

                for (seedPiece in seedNeighbours) {
                    val seedCenter = seedBodyCenters["${seedPiece.column}:${seedPiece.row}"]!!
                    val dCol = (otherPiece.column - seedPiece.column).toDouble()
                    val dRow = (otherPiece.row - seedPiece.row).toDouble()
                    val expectedX = seedCenter.first + dCol * pieceWidth * cos - dRow * pieceHeight * sin
                    val expectedY = seedCenter.second + dCol * pieceWidth * sin + dRow * pieceHeight * cos

                    val dx = otherWorldCenter.first - expectedX
                    val dy = otherWorldCenter.second - expectedY
                    val distance = max(abs(dx), abs(dy))
                    if (distance >= SNAP_TOLERANCE_PIXELS) continue

                    // Correction moves the SEED by (dx, dy) so its neighbour lands on the other piece.
                    val existing = bestByGroupId[otherGroup.id]
                    if (existing == null || distance < existing.distance) {
                        bestByGroupId[otherGroup.id] = BestPerGroup(dx, dy, distance)
                    }
                }
            }
        }

        return bestByGroupId.map { (groupId, best) ->
            val otherGroup = status.groups.first { it.id == groupId }
            SnapCandidate(otherGroup, best.correctionX, best.correctionY, best.distance)
        }
    }

    /**
     * Merge seed and partners into one group. Pieces retain their world body centers
     * (seed already carries the snap correction); the new pivot = centroid of world body centers
     * so the group's (x, y) is consistent with the shared-formula used by bodyCenter(...).
     *
     * Surviving groupId = smallest id by string compare across {seed.id} ∪ partner ids.
     */
    private fun mergeGroups(
        seed: JigsawGroup,
        partners: List<JigsawGroup>,
        pieceWidth: Double,
        pieceHeight: Double,
    ): JigsawGroup {
        val allGroups = listOf(seed) + partners
        val survivingId = allGroups.map { it.id }.min()

        val allBodyCenters = allGroups.flatMap { group ->
            group.pieces.map { piece -> bodyCenter(group, piece, pieceWidth, pieceHeight) }
        }
        val pivotX = allBodyCenters.sumOf { it.first } / allBodyCenters.size
        val pivotY = allBodyCenters.sumOf { it.second } / allBodyCenters.size

        val mergedPieces = allGroups.flatMap { it.pieces }

        return JigsawGroup(
            id = survivingId,
            pieces = mergedPieces,
            rotation = seed.rotation,
            x = pivotX,
            y = pivotY,
        )
    }

    // --- Geometry helpers -----------------------------------------------

    private fun pieceWidth(columns: Int): Double = JIGSAW_IMAGE_WIDTH * JIGSAW_PUZZLE_SCALE / columns
    private fun pieceHeight(rows: Int): Double = JIGSAW_IMAGE_HEIGHT * JIGSAW_PUZZLE_SCALE / rows

    /**
     * Compute the world-space body center of a piece in the given group. Invariant:
     *   pivot = mean of all group pieces' body centers in grid-rotated space.
     * So each piece's center = pivot + rotate(gridOffsetFromGridCentroid).
     *
     * This matches the frontend model where `container.position == container.pivot == centroid`
     * and children are rigidly offset in their un-rotated grid positions.
     */
    private fun bodyCenter(group: JigsawGroup, piece: GridPosition, pieceWidth: Double, pieceHeight: Double): Pair<Double, Double> {
        val gridCentroidX = group.pieces.sumOf { it.column.toDouble() } / group.pieces.size * pieceWidth
        val gridCentroidY = group.pieces.sumOf { it.row.toDouble() } / group.pieces.size * pieceHeight
        val ox = piece.column * pieceWidth - gridCentroidX
        val oy = piece.row * pieceHeight - gridCentroidY

        val angleRad = Math.toRadians(group.rotation.toDouble())
        val cos = cos(angleRad)
        val sin = sin(angleRad)
        val rx = ox * cos - oy * sin
        val ry = ox * sin + oy * cos
        return Pair(group.x + rx, group.y + ry)
    }
}
