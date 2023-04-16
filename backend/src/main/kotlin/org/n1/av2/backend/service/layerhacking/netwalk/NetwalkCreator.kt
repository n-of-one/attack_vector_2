package org.n1.av2.backend.service.layerhacking.netwalk

import org.n1.av2.backend.entity.ice.*
import org.n1.av2.backend.entity.site.enums.IceStrength
import kotlin.random.Random

class Wall(
    val from: Point,
    val to: Point
) {

// Walls must be agnostic of ordering of from and to

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

class NetwalkCreator(val iceStrength: IceStrength) {

    companion object {
        val sizeByStrength = mapOf(
            IceStrength.VERY_WEAK to 5,
            IceStrength.WEAK to 9,
            IceStrength.AVERAGE to 11,
            IceStrength.STRONG to 13,
            IceStrength.VERY_STRONG to 9,
            IceStrength.ONYX to 11,
        )
    }

    private val size = sizeByStrength[iceStrength]!!
    private val wrapping = (iceStrength == IceStrength.VERY_STRONG || iceStrength == IceStrength.ONYX)

    private val grid: MutableSet<Point> = mutableSetOf()
    private val walls: MutableSet<Wall> = mutableSetOf()
    private val finalWalls: MutableSet<Wall> = createStartFinalWalls()

    private fun createStartFinalWalls(): MutableSet<Wall> {
        if (wrapping) return mutableSetOf()

        val result = mutableSetOf<Wall>()
        (0 until size).forEach { x ->
            result.add(Wall(Point(x, 0), Point(x, -1)))
            result.add(Wall(Point(x, size - 1), Point(x, size)))
        }
        (0 until size).forEach { y ->
            result.add(Wall(Point(0, y), Point(-1, y)))
            result.add(Wall(Point(size - 1, y), Point(size, y)))
        }
        return result
    }


    fun create(): List<List<NetwalkCellMinimal>> {
        val center = (size) / 2
        val start = Point(center, center)
        walls.addAll(findWalls(start))
        grid.add(start)

        while (walls.isNotEmpty()) {
            printGrid(walls, finalWalls)

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

        val result: MutableList<MutableList<NetwalkCellMinimal>> = mutableListOf()

        for (y in 0 until size) {
            val row: MutableList<NetwalkCellMinimal> = mutableListOf()
            for (x in 0 until size) {
                val type = cellType(x, y, walls, finalWalls) ?: error("No cell type for $x, $y")

                val rotations = Random.nextInt(4)
                var rotatedType = type
                (0 until rotations).forEach { rotatedType = rotatedType.rotateClockwise() }

                val cell = NetwalkCellMinimal(
                    type = rotatedType,
                    connected = false
                )
                row.add(cell)
            }
            result.add(row)
        }

        return NetwalkConnectionTracer(result).trace().second
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
            toX %= size
            toY %= size
        } else {
            if (toX < 0 || toX >= size || toY < 0 || toY >= size) {
                return null
            }
        }
        return Wall(from, Point(toX, toY))
    }

    private fun printGrid(walls: Set<Wall>, finalWalls: Set<Wall>) {
        for (y in 0 until size) {
            for (x in 0 until size) {
                val image = cellType(x, y, walls, finalWalls)?.textImage ?: '*'
                print(image)
            }
            println()
        }
        println()
    }

    private fun cellType(x: Int, y: Int, walls: Set<Wall>, finalWalls: Set<Wall>): NetwalkCellType? {
        val point = Point(x, y)
        val nWall = Wall(point, Point(x, y - 1))
        val eWall = Wall(point, Point(x + 1, y))
        val sWall = Wall(point, Point(x, y + 1))
        val wWall = Wall(point, Point(x - 1, y))

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
}