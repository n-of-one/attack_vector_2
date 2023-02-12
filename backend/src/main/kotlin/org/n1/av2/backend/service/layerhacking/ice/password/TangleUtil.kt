package org.n1.av2.backend.service.layerhacking.ice.password

import java.util.*

fun intersect(line1: TLine, line2: TLine): TPoint {
    val x = (line2.yOffset - line1.yOffset) / (line1.slope - line2.slope)
    val y = line1.slope * x + line1.yOffset
    return TPoint(x, y)
}

fun toLine(x1Input: Float, y1: Float, x2: Float, y2: Float): TLine {

    val x1 = if (x1Input == x2) x1Input + 1 else x1Input

    // prevent infinite slope lines via heuristic

    val slope = (y2 - y1) / (x2 - x1)
    val offset = y1 - x1 * slope

    return TLine(slope, offset, LinkedList(), x1, y1, x2, y2)
}

fun segmentsIntersect(p0_x: Int, p0_y: Int, p1_x: Int, p1_y: Int,
                      p2_x: Int, p2_y: Int, p3_x: Int, p3_y: Int): Boolean {
    val s1_x = (p1_x - p0_x).toFloat()
    val s1_y = (p1_y - p0_y).toFloat()
    val s2_x = (p3_x - p2_x).toFloat()
    val s2_y = (p3_y - p2_y).toFloat()

    val s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y)
    val t = (s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y)

    if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
        val i_x = p0_x + (t * s1_x)
        val i_y = p0_y + (t * s1_y)
        println("Intersect at (${i_x}. ${i_y}")
        return true
    }
    return false
}