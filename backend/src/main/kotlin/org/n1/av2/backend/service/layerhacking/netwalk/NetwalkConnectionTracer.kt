package org.n1.av2.backend.service.layerhacking.netwalk

import org.n1.av2.backend.entity.ice.Direction
import org.n1.av2.backend.entity.ice.NetwalkCellMinimal
import org.n1.av2.backend.entity.ice.Point

class NetwalkConnectionTracer(
    val cellGrid: List<List<NetwalkCellMinimal>>,
) {

    private val connected = mutableListOf<Point>()

    fun trace(): Pair<List<Point>, List<List<NetwalkCellMinimal>>> {
        val size = cellGrid.size
        val center = size / 2

        walk(center, center, null)

        val updatedGrid = cellGrid.mapIndexed { y, row: List<NetwalkCellMinimal> ->
            row.mapIndexed { x, cell ->
                cell.copy(connected = connected.contains(Point(x, y)))
            }
        }

        return Pair(connected, updatedGrid)
    }

    private fun walk(x: Int, y: Int, fromDirection: Direction?) {
        if (connected.contains(Point(x, y))) {
            return
        }

        if (x < 0 || y < 0 || x >= cellGrid.size || y >= cellGrid.size) {
            return
        }

        val cell = cellGrid[y][x]
        val type = cell.type

        if ((fromDirection == null) ||
            (fromDirection == Direction.N && type.s) ||
            (fromDirection == Direction.E && type.w) ||
            (fromDirection == Direction.S && type.n) ||
            (fromDirection == Direction.W && type.e)) {

            connected.add(Point(x, y))

            if (cell.type.n) {
                walk(x, y - 1, Direction.N)
            }
            if (cell.type.e) {
                walk(x + 1, y, Direction.E)
            }
            if (cell.type.s) {
                walk(x, y + 1, Direction.S)
            }
            if (cell.type.w) {
                walk(x - 1, y, Direction.W)
            }
        }
    }

}