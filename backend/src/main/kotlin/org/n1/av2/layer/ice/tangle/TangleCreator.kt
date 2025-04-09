package org.n1.av2.layer.ice.tangle

import org.n1.av2.site.entity.enums.IceStrength
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random


/**
 * Create an (un)tangle puzzle:
 * - generate a number of lines (L) that are non-parallel and no 3 lines intersect at the same position
 * - calculate the intersections. These are the points. There are 0.5 * L * (L-1) points
 * - the lines are also the connections between the points.
 */


class IdTPoint(val x: Float, val y: Float, val id: String, val clusterNumber: Int)

data class TConnection(val from: TPoint, val to: TPoint)

class TLine(val slope: Float, val yOffset: Float, val intersections: MutableList<TPoint>,
            val x1: Float, val y1: Float, val x2: Float, val y2: Float) {

    constructor(slope: Float, yOffset: Float) : this(slope, yOffset, LinkedList<TPoint>(), 0F, 0F, 0F, 0F)

    val ox = -100000F
    val oy = this.slope * ox + this.yOffset

    fun intersectWith(lines: List<TLine>) {
        lines.forEach { otherLine ->
            val intersection = intersect(this, otherLine)
            this.intersections.add(intersection)
            otherLine.intersections.add(intersection)
        }
    }

    fun sortIntersections() {
        intersections.sortBy { distanceSquared(it) }
    }

    private fun distanceSquared(point: TPoint): Float {

        val dx = ox - point.x
        val dy = oy - point.y

        return (dx * dx) + (dy * dy)
    }

    fun connections(): List<TConnection> {
        val connections = LinkedList<TConnection>()
        (0..(intersections.size - 2)).forEach { i ->
            val first = intersections[i]
            val second = intersections[i + 1]

            connections.add(TConnection(first, second))
        }
        return connections
    }
}


private var random = Random

class TangleCreation(val points: MutableList<TanglePoint>, val lines: List<TangleLine>)


class TangleCreator {

    fun create(strength: IceStrength): TangleCreation {
        val clusters = clusterCount(strength)

        val points = LinkedList<IdTPoint>()
        val tangleLines = LinkedList<TangleLine>()
        (1..clusters).forEach { clusterNumber ->
            val (clusterPoints, clusterLines) = createSingleTangle(clusterNumber, strength)
            points.addAll(clusterPoints)
            tangleLines.addAll(clusterLines)
        }

        // For debug: add the original line segments as points.
//        idPoints.addAll( createLinePoints(lines))
//        lines.forEachIndexed { index, line ->
//            tangleLines.add(TangleLine(500+index, 100+index, 200+index, TangleLineType.SETUP))
//        }

//      For debugging: layout the original points
//        val tanglePoints = layout(idPoints)

        points.shuffle()
        val tanglePoints = layoutAsCircle(points)

        println("tangle points: ${tanglePoints.size} for strength: $strength")
        return TangleCreation(tanglePoints.toMutableList(), tangleLines)
    }

    private fun createSingleTangle(clusterNumber: Int, strength: IceStrength): Pair<MutableList<IdTPoint>, MutableList<TangleLine>> {
        val lineCount = determineLineCount(strength)

        val lines = (1..lineCount).map { createLine() }

        val linesLeft = lines.toMutableList()
        while (!linesLeft.isEmpty()) {
            val line = linesLeft.removeAt(0)
            line.intersectWith(linesLeft)
        }

        val connections = LinkedList<TConnection>()
        // the intersections are sorted by distance so that we can connect them.
        lines.forEach { it.sortIntersections() }
        lines.forEach { connections.addAll(it.connections()) }

        // We have two TPoints for each x-y location.
        // so we create a unique TPoint for each x-y location
        val points = HashSet<TPoint>()
        lines.forEach { points.addAll(it.intersections) }
        val idPoints = points.mapIndexed { index, point -> IdTPoint(point.x, point.y, "p${clusterNumber}-${index}", clusterNumber) }.toMutableList()

        val tangleLines = connections.mapIndexed { index, connection ->
            toIdConnection(connection, idPoints, index)
        }.toMutableList()
        return Pair(idPoints, tangleLines)
    }

    private fun determineLineCount(strength: IceStrength): Int {
        return when (strength) {
            IceStrength.VERY_WEAK -> 5    // 10 points * 1 cluster  = 10 points
            IceStrength.WEAK -> 6         // 15 points * 2 clusters = 30 points
            IceStrength.AVERAGE -> 6      // 15 points * 3 clusters = 45 points
            IceStrength.STRONG -> 8       // 28 points * 3 clusters = 84 points
            IceStrength.VERY_STRONG -> 8  // 28 points * 4 clusters = 112 points
            IceStrength.ONYX -> 10        // 45 points * 4 clusters = 180 points
        }
    }

    private fun createLinePoints(lines: List<TLine>): List<IdTPoint> {
        val linePoints = LinkedList<IdTPoint>()
        lines.forEachIndexed { index, line ->
            linePoints.add(IdTPoint(line.x1, line.y1, "op-${line}-1", 1))
            linePoints.add(IdTPoint(line.x2, line.y2, "op-${index}-2", 1))
        }
        return linePoints
    }

    /**
     * For debugging: layout the points as the original construction lines.
     */
    private fun layoutOriginalConstruction(idPoints: List<IdTPoint>): List<TanglePoint> {
        val minX = idPoints.minBy { it.x }.x
        val maxX = idPoints.maxBy { it.x }.x
        val minY = idPoints.minBy { it.y }.y
        val maxY = idPoints.maxBy { it.y }.y
        val xSize = maxX - minX
        val ySize = maxY - minY

        val scaleX = xSize / 600
        val scaleY = ySize / 600



        return idPoints.map {
            TanglePoint("p${it.clusterNumber}-${it.id}",
                    (50 + (it.x - minX) / scaleX).roundToInt(),
                    (30 + (it.y - minY) / scaleY).roundToInt(),
                    it.clusterNumber
                )
        }
    }

    /**
     * Layout for actual puzzle
     */
    private fun layoutAsCircle(idPoints: List<IdTPoint>): List<TanglePoint> {
        val angleStep = (2 * Math.PI / idPoints.size)
        val tanglePoints = LinkedList<TanglePoint>()

        val padding = 20
        val xSize = 828
        val ySize = 828
        val canvasXSize = 1576
        val dx = (canvasXSize - xSize) / 2
        val xCenter = xSize / 2  + dx
        val yCenter = ySize / 2
        val xRadius = (xSize / 2) - padding
        val yRadius = (ySize / 2) - padding

        idPoints.forEachIndexed { index, idTPoint ->
            val angle = angleStep * index
            val x = (xCenter + xRadius * Math.sin(angle)).roundToInt()
            val y = (yCenter + yRadius * Math.cos(angle)).roundToInt()
            tanglePoints.add(TanglePoint(idTPoint.id, x, y, idTPoint.clusterNumber))
        }
        return tanglePoints
    }

    private fun toIdConnection(connection: TConnection, idPoints: List<IdTPoint>, index: Int): TangleLine {
        val idFrom = idPoints.find { it.x == connection.from.x && it.y == connection.from.y }!!
        val idTo = idPoints.find { it.x == connection.to.x && it.y == connection.to.y }!!
        return TangleLine("l-${index}", idFrom.id, idTo.id, TangleLineType.NORMAL)
    }


    private fun createLine(): TLine {
        val x1 = random.nextFloat() * 500
        val x2 = random.nextFloat() * 500
        val y1 = random.nextFloat() * 500
        val y2 = random.nextFloat() * 500

        return toLine(x1, y1, x2, y2)
    }

    companion object {

        fun clusterCount(strength: IceStrength): Int {
            return when (strength) {
                IceStrength.VERY_WEAK -> 1
                IceStrength.WEAK -> 2
                IceStrength.AVERAGE -> 3
                IceStrength.STRONG -> 3
                IceStrength.VERY_STRONG -> 4
                IceStrength.ONYX -> 4
            }
        }
    }


}
