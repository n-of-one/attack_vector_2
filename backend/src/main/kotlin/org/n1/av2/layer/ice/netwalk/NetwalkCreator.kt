package org.n1.av2.layer.ice.netwalk

import org.n1.av2.site.entity.enums.IceStrength
import kotlin.random.Random

/**
 * https://en.wikipedia.org/wiki/Maze_generation_algorithm
 * https://www.youtube.com/watch?v=XajeSjilUDs&t=124s
 *
 * Randomized Prim's algorithm
 * This algorithm is a randomized version of Prim's algorithm.
 *
 * - Start with a grid full of walls.
 * - Pick a cell, mark it as part of the maze. Add the walls of the cell to the wall list.
 * While there are walls in the list:
 * - Pick a random wall from the list. If only one of the two cells that the wall divides is visited, then:
 * - Make the wall a passage and mark the unvisited cell as part of the maze.
 * - Add the neighboring walls of the cell to the wall list.
 * - Remove the wall from the list.
 * It will usually be relatively easy to find the way to the starting cell, but hard to find the way anywhere else.
*/



class Wall(
    val from: Point,
    val to: Point
) {

    // Walls must be agnostic of ordering of from and to
    // This is important for finding if a wall is already in a set of walls.

    override fun equals(other: Any?): Boolean {
        if (other !is Wall) return false
        return (from == other.from && to == other.to) || (from == other.to && to == other.from)
    }

    override fun hashCode(): Int {
        val maxX = maxOf(from.x, to.x)
        val minX = minOf(from.x, to.x)
        val maxY = maxOf(from.y, to.y)
        val minY = minOf(from.y, to.y)

        return Point(minX + maxX * 100, minY + maxY * 100).hashCode()
    }
}

class NetwalkPuzzle(val grid: List<List<NetwalkCell>>, val wrapping: Boolean)


/**
 * This implementation either creates a netwalk puzzle, or returns null if the solution
 * - has unreachable positions
 * - ends up with a solution where the middle is an end leaf
 */
class NetwalkCreator(iceStrength: IceStrength) {

    companion object {
        val sizeByStrength = mapOf(
            IceStrength.VERY_WEAK to Pair (7, 7),
            IceStrength.WEAK to Pair (9, 9),
            IceStrength.AVERAGE to Pair (11, 11),
            IceStrength.STRONG to Pair (15, 13),
            IceStrength.VERY_STRONG to Pair (9, 9),
            IceStrength.ONYX to Pair(17, 11)
        )
    }

    private val sizeX = sizeByStrength[iceStrength]!!.first
    private val sizeY = sizeByStrength[iceStrength]!!.second
    private val wrapping = (iceStrength == IceStrength.VERY_STRONG || iceStrength == IceStrength.ONYX)

    private val grid: MutableSet<Point> = mutableSetOf()
    private val walls: MutableSet<Wall> = mutableSetOf()
    private val finalWalls: MutableSet<Wall> = createStartFinalWalls()

    private fun createStartFinalWalls(): MutableSet<Wall> {
        if (wrapping) return mutableSetOf()

        val result = mutableSetOf<Wall>()
        (0 until sizeX).forEach { x ->
            result.add(Wall(Point(x, 0), Point(x, -1)))
            result.add(Wall(Point(x, sizeY - 1), Point(x, sizeY)))
        }
        (0 until sizeY).forEach { y ->
            result.add(Wall(Point(0, y), Point(-1, y)))
            result.add(Wall(Point(sizeX - 1, y), Point(sizeX, y)))
        }
        return result
    }

    fun create(): NetwalkPuzzle? {
        createPuzzle()
        val result = convertPuzzleToGrid() ?: return null
        if (centerIsALeaf(result)) return null

        val grid = NetwalkConnectionTracer(result, wrapping).trace().second
        return NetwalkPuzzle(grid, wrapping)
    }

    private fun convertPuzzleToGrid(): List<List<NetwalkCell>>? {
        val result: MutableList<MutableList<NetwalkCell>> = mutableListOf()

        for (y in 0 until sizeY) {
            val row: MutableList<NetwalkCell> = mutableListOf()
            for (x in 0 until sizeX) {
                val type = cellType(x, y, walls, finalWalls) ?: return null

                val rotations = Random.nextInt(4)
                var rotatedType = type
                repeat(rotations) { rotatedType = rotatedType.rotateClockwise() }

                val cell = NetwalkCell(
                    type = rotatedType,
                    connected = false
                )
                row.add(cell)
            }
            result.add(row)
        }
        return result
    }

    private fun createPuzzle() {
        val start = Point(Random.nextInt(sizeX), Random.nextInt(sizeY))
        walls.addAll(findWalls(start))
        grid.add(start)

        while (walls.isNotEmpty()) {
    //            printGrid(walls, finalWalls)

            val wall = walls.random()
            if (grid.contains(wall.to)) {
                // The other side of the wall is already part of the maze.
                // This wall will stay in the maze
                finalWalls.add(wall)
            } else {
                // connect to the other side.
                grid.add(wall.to)

                val newWalls = findWalls(wall.to).toMutableList()
                newWalls.removeAll(finalWalls)
                newWalls.remove(wall)
                walls.addAll(newWalls)
            }
            walls.remove(wall)
        }
    }

    private fun findWalls(location: Point): List<Wall> {
        val walls = mutableListOf<Wall?>()

        walls.add(wallOrNull(location, Direction.N))
        walls.add(wallOrNull(location, Direction.E))
        walls.add(wallOrNull(location, Direction.S))
        walls.add(wallOrNull(location, Direction.W))

        return walls.filterNotNull()
    }

    private fun wallOrNull(from: Point, direction: Direction): Wall? {
        var toX = from.x + direction.dx
        var toY = from.y + direction.dy

        if (wrapping) {
            toX = wrapX(toX)
            toY = wrapY(toY)
        } else {
            if (toX < 0 || toX >= sizeX || toY < 0 || toY >= sizeY) {
                return null
            }
        }
        return Wall(from, Point(toX, toY))
    }

    private fun printGrid(walls: Set<Wall>, finalWalls: Set<Wall>) {
        for (y in 0 until sizeY) {
            for (x in 0 until sizeX) {
                val image = cellType(x, y, walls, finalWalls)?.textImage ?: '*'
                print(image)
            }
            println()
        }
        println()
    }

    private fun cellType(x: Int, y: Int, walls: Set<Wall>, finalWalls: Set<Wall>): NetwalkCellType? {
        val point = Point(x, y)
        val nWall = Wall(point, Point(x, wrapY(y - 1)))
        val eWall = Wall(point, Point(wrapX(x + 1), y))
        val sWall = Wall(point, Point(x, wrapY(y + 1)))
        val wWall = Wall(point, Point(wrapX(x - 1), y))

        val nHasWall = walls.contains(nWall) || finalWalls.contains(nWall)
        val eHasWall = walls.contains(eWall) || finalWalls.contains(eWall)
        val sHasWall = walls.contains(sWall) || finalWalls.contains(sWall)
        val wHasWall = walls.contains(wWall) || finalWalls.contains(wWall)

        return NetwalkCellType.values().find { type ->
            type.n != nHasWall &&
                    type.e != eHasWall &&
                    type.s != sHasWall &&
                    type.w != wHasWall
        }
    }

    private fun wrapX(x: Int): Int {
        if (!wrapping) return x
        return (sizeX + x) % sizeX
    }

    private fun wrapY(y: Int): Int {
        if (!wrapping) return y
        return (sizeY + y) % sizeY
    }

    private fun centerIsALeaf(grid: List<List<NetwalkCell>>): Boolean {
        val center = Point(sizeX / 2, sizeY / 2)
        val type = grid[center.y][center.x].type
        return (type == NetwalkCellType.N || type == NetwalkCellType.E || type == NetwalkCellType.S || type == NetwalkCellType.W)
    }

}
