package org.n1.av2.layer.ice.jigsaw

import org.n1.av2.site.entity.enums.IceStrength
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Builds a fresh Jigsaw puzzle: piece geometry, initial scatter positions, random rotations,
 * and one single-piece group per piece.
 *
 * Port of frontend/src/standalone/ice/jigsaw/component/JigsawShapes.ts — values and logic must
 * match the client exactly so snap tolerances and visuals line up.
 */

// Canvas (play area) — must match frontend CANVAS_WIDTH / CANVAS_HEIGHT.
const val JIGSAW_CANVAS_WIDTH = 1880
const val JIGSAW_CANVAS_HEIGHT = 928

// Image dimensions and scale — must match frontend IMAGE_WIDTH / IMAGE_HEIGHT / PUZZLE_SCALE.
const val JIGSAW_IMAGE_WIDTH = 1376
const val JIGSAW_IMAGE_HEIGHT = 768
const val JIGSAW_PUZZLE_SCALE = 0.8

private const val TAB_HEIGHT_RATIO = 0.08

private val ALL_SHAPES = listOf("invected", "embattled", "indented", "raguly")
private val ROTATIONS = listOf(0, 90, 180, 270)
private val FLAT_EDGE = EdgeConfig(shape = null, direction = "flat")

data class JigsawGrid(val columns: Int, val rows: Int)

private val GRID_FOR_STRENGTH: Map<IceStrength, JigsawGrid> = mapOf(
    IceStrength.VERY_WEAK to JigsawGrid(5, 3),
    IceStrength.WEAK to JigsawGrid(7, 4),
    IceStrength.AVERAGE to JigsawGrid(10, 6),
    IceStrength.STRONG to JigsawGrid(13, 8),
    IceStrength.VERY_STRONG to JigsawGrid(17, 10),
    IceStrength.ONYX to JigsawGrid(21, 12),
)

class JigsawCreation(
    val columns: Int,
    val rows: Int,
    val imageSource: String,
    val pieces: List<PieceConfig>,
    val groups: List<JigsawGroup>,
)

class JigsawCreator(testingMode: Boolean) {

    private val random: Random = if (testingMode) Random(0L) else Random.Default

    fun create(strength: IceStrength): JigsawCreation {
        val grid = GRID_FOR_STRENGTH[strength] ?: error("No jigsaw grid for strength $strength")
        val (columns, rows) = grid

        val imageSource = pickImageSource(strength)

        val pieceWidth = JIGSAW_IMAGE_WIDTH * JIGSAW_PUZZLE_SCALE / columns
        val pieceHeight = JIGSAW_IMAGE_HEIGHT * JIGSAW_PUZZLE_SCALE / rows
        val maxTabSize = max(pieceWidth, pieceHeight) * TAB_HEIGHT_RATIO

        // horizontalEdges[row][column] = edge between (column, row).right and (column+1, row).left
        val horizontalEdges: List<List<EdgeConfig>> = (0 until rows).map {
            (0 until columns - 1).map { randomShapedEdge() }
        }
        // verticalEdges[row][column] = edge between (column, row).bottom and (column, row+1).top
        val verticalEdges: List<List<EdgeConfig>> = (0 until rows - 1).map {
            (0 until columns).map { randomShapedEdge() }
        }

        val positions = generateSurroundingPositions(
            count = columns * rows,
            pieceWidth = pieceWidth,
            pieceHeight = pieceHeight,
            maxTabSize = maxTabSize,
            canvasWidth = JIGSAW_CANVAS_WIDTH.toDouble(),
            canvasHeight = JIGSAW_CANVAS_HEIGHT.toDouble(),
        )

        val pieces = mutableListOf<PieceConfig>()
        val groups = mutableListOf<JigsawGroup>()

        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val top = if (row == 0) FLAT_EDGE else flipDirection(verticalEdges[row - 1][column])
                val bottom = if (row == rows - 1) FLAT_EDGE else verticalEdges[row][column]
                val left = if (column == 0) FLAT_EDGE else flipDirection(horizontalEdges[row][column - 1])
                val right = if (column == columns - 1) FLAT_EDGE else horizontalEdges[row][column]

                pieces.add(PieceConfig(column = column, row = row, top = top, right = right, bottom = bottom, left = left))

                val index = row * columns + column
                val (x, y) = positions[index]
                val rotation = ROTATIONS[random.nextInt(4)]
                val groupId = "g-$index"
                groups.add(
                    JigsawGroup(
                        id = groupId,
                        pieces = listOf(GridPosition(column, row)),
                        rotation = rotation,
                        x = x,
                        y = y,
                    )
                )
            }
        }

        return JigsawCreation(columns, rows, imageSource, pieces, groups)
    }

    private fun pickImageSource(strength: IceStrength): String {
        val options = DEFAULT_MEDIA[strength] ?: DEFAULT_MEDIA[IceStrength.AVERAGE]!!
        return options[random.nextInt(options.size)]
    }

    private fun randomShapedEdge(): EdgeConfig {
        val shape = ALL_SHAPES[random.nextInt(ALL_SHAPES.size)]
        val direction = if (random.nextDouble() < 0.5) "out" else "in"
        return EdgeConfig(shape = shape, direction = direction)
    }

    private fun flipDirection(edge: EdgeConfig): EdgeConfig {
        if (edge.direction == "flat") return FLAT_EDGE
        val flipped = if (edge.direction == "out") "in" else "out"
        return EdgeConfig(shape = edge.shape, direction = flipped)
    }

    /**
     * Distribute pieces in 4 zones (left, right, top, bottom) around the centered puzzle area.
     * Port of JigsawShapes.ts:generateSurroundingPositions — behaviour must match so the
     * initial scatter looks right.
     */
    private fun generateSurroundingPositions(
        count: Int,
        pieceWidth: Double,
        pieceHeight: Double,
        maxTabSize: Double,
        canvasWidth: Double,
        canvasHeight: Double,
    ): List<Pair<Double, Double>> {

        val displayWidth = JIGSAW_IMAGE_WIDTH * JIGSAW_PUZZLE_SCALE
        val displayHeight = JIGSAW_IMAGE_HEIGHT * JIGSAW_PUZZLE_SCALE
        val puzzleCenterX = canvasWidth / 2
        val puzzleCenterY = canvasHeight / 2

        val puzzleLeft = puzzleCenterX - displayWidth / 2
        val puzzleRight = puzzleCenterX + displayWidth / 2
        val puzzleTop = puzzleCenterY - displayHeight / 2
        val puzzleBottom = puzzleCenterY + displayHeight / 2

        val edgeMargin = maxTabSize
        val maxPieceDim = max(pieceWidth, pieceHeight)
        val halfExtent = (maxPieceDim + 2 * maxTabSize) / 2

        val minZoneSize = max(pieceWidth, pieceHeight) * 0.5

        val leftNaturalW = puzzleLeft - edgeMargin
        val leftSafeW = puzzleLeft - halfExtent - edgeMargin
        val leftW = if (leftSafeW >= minZoneSize) leftSafeW else max(minZoneSize, leftNaturalW)

        val rightNaturalX = puzzleRight
        val rightSafeX = puzzleRight + halfExtent
        val rightNaturalW = canvasWidth - edgeMargin - puzzleRight
        val rightSafeW = canvasWidth - edgeMargin - rightSafeX
        val useRightSafe = rightSafeW >= minZoneSize
        val rightX = if (useRightSafe) rightSafeX else min(rightNaturalX, canvasWidth - edgeMargin - minZoneSize)
        val rightW = if (useRightSafe) rightSafeW else max(minZoneSize, rightNaturalW)

        val topNaturalH = puzzleTop - edgeMargin
        val topSafeH = puzzleTop - halfExtent - edgeMargin
        val topH = if (topSafeH >= minZoneSize) topSafeH else max(minZoneSize, topNaturalH)

        val bottomNaturalY = puzzleBottom
        val bottomSafeY = puzzleBottom + halfExtent
        val bottomNaturalH = canvasHeight - edgeMargin - puzzleBottom
        val bottomSafeH = canvasHeight - edgeMargin - bottomSafeY
        val useBottomSafe = bottomSafeH >= minZoneSize
        val bottomY = if (useBottomSafe) bottomSafeY else min(bottomNaturalY, canvasHeight - edgeMargin - minZoneSize)
        val bottomH = if (useBottomSafe) bottomSafeH else max(minZoneSize, bottomNaturalH)

        data class Rect(val x: Double, val y: Double, val w: Double, val h: Double)

        val zones = listOf(
            // Left
            Rect(edgeMargin, edgeMargin, leftW, canvasHeight - edgeMargin * 2),
            // Right
            Rect(rightX, edgeMargin, rightW, canvasHeight - edgeMargin * 2),
            // Top
            Rect(puzzleLeft, edgeMargin, max(minZoneSize, puzzleRight - puzzleLeft), topH),
            // Bottom
            Rect(puzzleLeft, bottomY, max(minZoneSize, puzzleRight - puzzleLeft), bottomH),
        )

        val totalArea = zones.sumOf { it.w * it.h }
        val positions = mutableListOf<Pair<Double, Double>>()

        var remaining = count
        for (i in zones.indices) {
            val zone = zones[i]
            val zonePieceCount = if (i == zones.size - 1) remaining
            else (count * (zone.w * zone.h) / totalArea).roundToInt()
            remaining -= zonePieceCount
            if (zonePieceCount <= 0) continue

            val zoneAspect = zone.w / zone.h
            val gridRows = max(1, sqrt(zonePieceCount / zoneAspect).roundToInt())
            val gridCols = max(1, ceil(zonePieceCount.toDouble() / gridRows).toInt())

            val spacingX = if (gridCols > 1) zone.w / gridCols else 0.0
            val spacingY = if (gridRows > 1) zone.h / gridRows else 0.0

            val jitterX = spacingX * 0.15
            val jitterY = spacingY * 0.15

            for (j in 0 until zonePieceCount) {
                val gridCol = j % gridCols
                val gridRow = j / gridCols
                val baseX = zone.x + (gridCol + 0.5) * spacingX
                val baseY = zone.y + (gridRow + 0.5) * spacingY
                positions.add(
                    Pair(
                        baseX + (random.nextDouble() - 0.5) * 2 * jitterX,
                        baseY + (random.nextDouble() - 0.5) * 2 * jitterY,
                    )
                )
            }
        }

        // Shuffle so pieces from the same grid row aren't all in the same zone
        for (i in positions.size - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = positions[i]
            positions[i] = positions[j]
            positions[j] = tmp
        }

        // Clamp to stay fully on canvas
        return positions.map { (x, y) ->
            Pair(
                x.coerceIn(halfExtent, canvasWidth - halfExtent),
                y.coerceIn(halfExtent, canvasHeight - halfExtent),
            )
        }
    }
}
