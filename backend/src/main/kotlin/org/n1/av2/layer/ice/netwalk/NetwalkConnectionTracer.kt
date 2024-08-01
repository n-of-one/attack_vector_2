package org.n1.av2.layer.ice.netwalk

/**
 * Trace a path through the netwalk grid, starting from the center. Any cells that are connected to the center will be
 * marked as connected.
 */

class NetwalkConnectionTracer(
    val cellGrid: List<List<NetwalkCell>>,
    val wrapping: Boolean,
) {

    private val connected = mutableListOf<Point>()

    private val sizeX = cellGrid[0].size
    private val sizeY = cellGrid.size


    fun trace(): Pair<List<Point>, List<List<NetwalkCell>>> {
        val centerX = sizeX / 2
        val centerY = sizeY / 2

        walk(centerX, centerY, null)

        val updatedGrid = cellGrid.mapIndexed { y, row: List<NetwalkCell> ->
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

        if (x < 0 || y < 0 || x >= sizeX || y >= sizeY) return


        val cell = cellGrid[y][x]
        val type = cell.type

        if ((fromDirection == null) ||
            (fromDirection == Direction.N && type.s) ||
            (fromDirection == Direction.E && type.w) ||
            (fromDirection == Direction.S && type.n) ||
            (fromDirection == Direction.W && type.e)) {

            connected.add(Point(x, y))

            if (cell.type.n) {
                walk(x, wrapY(y - 1), Direction.N)
            }
            if (cell.type.e) {
                walk(wrapX(x + 1), y, Direction.E)
            }
            if (cell.type.s) {
                walk(x, wrapY(y + 1), Direction.S)
            }
            if (cell.type.w) {
                walk(wrapX(x - 1), y, Direction.W)
            }
        }
    }


    fun wrapX(x: Int): Int {
        if (!wrapping) return x
        return (sizeX + x) % sizeX
    }

    fun wrapY(y: Int): Int {
        if (!wrapping) return y
        return (sizeY + y) % sizeY
    }
}
