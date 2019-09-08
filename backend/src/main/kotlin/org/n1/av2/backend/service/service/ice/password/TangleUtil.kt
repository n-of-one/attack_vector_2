package org.n1.av2.backend.service.service.ice.password

import java.util.*

fun intersect(line1: TLine, line2: TLine): TPoint {
    val x = (line2.yOffset - line1.yOffset) / (line1.slope - line2.slope)
    val y = line1.slope * x + line1.yOffset
    return TPoint(x, y)
}

fun toLine(x1Input: Float, y1: Float, x2: Float, y2: Float): TLine {

    val x1 =     if (x1Input == x2) x1Input+1 else x1Input

    // prevent infinite slope lines via heuristic

    val slope = (y2 - y1) / (x2 - x1)
    val offset = y1 - x1 * slope

    return TLine(slope, offset, LinkedList(), x1, y1, x2, y2)
}