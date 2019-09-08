package org.n1.av2.backend.service.service.ice.password

import org.n1.av2.backend.model.db.run.TangleLine
import org.n1.av2.backend.model.db.run.TangleLineType
import org.n1.av2.backend.model.db.run.TanglePoint
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random


/**
 * Create an (un)tangle puzzle:
 * - generate a number of lines (L) that are non-parallel and no 3 lines intersect at the same position
 * - calculate the intersections. These are the points. There are 0.5 * L * (L-1) points
 * - the lines are also the connections between the points. So if Lin
 */



class IdTPoint(val x: Float, val y: Float, val id: Int)
data class TConnection(val from: TPoint, val to: TPoint)

class TLine(val slope: Float, val yOffset: Float, val intersections: MutableList<TPoint>,
                    val x1: Float, val y1: Float, val x2: Float, val y2: Float) {

    constructor(slope: Float, yOffset: Float): this(slope, yOffset, LinkedList<TPoint>(), 0F, 0F, 0F, 0F)

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
        println("Sorted:")
        intersections.forEach { println(it) }
    }

    fun distanceSquared(point: TPoint): Float {

        val dx = ox - point.x
        val dy = oy - point.y

        return (dx * dx) +  (dy * dy)
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


private var random = Random(0)

class TangleCreation(val points: MutableList<TanglePoint>, val lines: List<TangleLine>)


class TangleCreator {

    fun create(lineCount: Int): TangleCreation {
        random = Random(0)
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
        val idPoints = points.mapIndexed { index, point -> IdTPoint(point.x, point.y, index) }.toMutableList()

        val tangleLines = connections.mapIndexed { index, connection -> toIdConnection(connection, idPoints, index) }.toMutableList()



        // FIXME
//        idPoints.addAll( createLinePoints(lines))
//        lines.forEachIndexed { index, line ->
//            tangleLines.add(TangleLine(500+index, 100+index, 200+index, TangleLineType.SETUP))
//        }

        val tanglePoints = layoutForStart(idPoints.size)


//      For debugging: layout the original points
//        val tanglePoints = layout(idPoints)

        return TangleCreation(tanglePoints.toMutableList(), tangleLines)
    }

    private fun createLinePoints(lines: List<TLine>): List<IdTPoint> {
        val linePoints = LinkedList<IdTPoint>()
        lines.forEachIndexed { index, line ->
            linePoints.add(IdTPoint(line.x1, line.y1, 100+index))
            linePoints.add(IdTPoint(line.x2, line.y2, 200+index))
        }
        return linePoints
    }

    private fun layout(idPoints: List<IdTPoint>): List<TanglePoint> {
        val minX = idPoints.minBy { it.x }!!.x
        val maxX = idPoints.maxBy { it.x }!!.x
        val minY = idPoints.minBy { it.y }!!.y
        val maxY = idPoints.maxBy { it.y }!!.y
        val xSize = maxX - minX
        val ySize = maxY - minY

        val scaleX = xSize / 600
        val scaleY = ySize / 600



        return idPoints.map {
            TanglePoint(it.id,
                    (50 + (it.x - minX) / scaleX).roundToInt(),
                    (30 + (it.y - minY) / scaleY).roundToInt())
        }
    }

    private fun layoutForStart(size: Int): List<TanglePoint> {
        val angleStep = (2 * Math.PI / size)
        val tanglePoints = LinkedList<TanglePoint>()
        (0..size - 1).forEach { index ->
            val angle = angleStep * index
            val x = (300 + 200 * Math.sin(angle)).roundToInt()
            val y = (300 + 200 * Math.cos(angle)).roundToInt()
            tanglePoints.add(TanglePoint(index, x, y))
        }
        return tanglePoints
    }

    private fun toIdConnection(connection: TConnection, idPoints: List<IdTPoint>, index: Int): TangleLine {
        val idFrom = idPoints.find { it.x == connection.from.x && it.y == connection.from.y }!!
        val idTo = idPoints.find { it.x == connection.to.x && it.y == connection.to.y }!!
        return TangleLine(index, idFrom.id, idTo.id, TangleLineType.NORMAL)
    }


    private fun createLine(): TLine {
        val x1 = random.nextFloat() * 500
        val x2 = random.nextFloat() * 500
        val y1 = random.nextFloat() * 500
        val y2 = random.nextFloat() * 500

        return toLine(x1, y1, x2, y2)
    }


}