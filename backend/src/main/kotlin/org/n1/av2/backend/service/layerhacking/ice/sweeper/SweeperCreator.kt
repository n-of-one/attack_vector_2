package org.n1.av2.backend.service.layerhacking.ice.sweeper

import org.n1.av2.backend.entity.ice.SweeperIceStatus
import org.n1.av2.backend.entity.site.enums.IceStrength


class SweeperCreator {

    fun createSweeper(iceId: String, layerId: String, iceStrength: IceStrength): SweeperIceStatus {
        val map = createMap(iceStrength)
        val cells = map.cells()
        val modifiers = map.modifiers()
        return SweeperIceStatus(iceId, layerId, iceStrength, cells, modifiers, false)
    }

    private fun createMap(iceStrength: IceStrength): SweeperMap {
        return when (iceStrength) {
            IceStrength.VERY_WEAK -> SweeperMap(9, 9, 10) // Beginner
            IceStrength.WEAK -> SweeperMap(12, 12, 20)
            IceStrength.AVERAGE -> SweeperMap(16, 16, 40) // Intermediate
            IceStrength.STRONG -> SweeperMap(22, 16, 60)
            IceStrength.VERY_STRONG -> SweeperMap(30, 16, 100) // Expert
            IceStrength.ONYX -> SweeperMap(32, 18, 120)
        }
    }
}

class SweeperMap(
    private val xSize: Int,
    private val ySize: Int,
    mineCount: Int,
) {

    private val minePositions = generateMinePositions(mineCount)

    private fun generateMinePositions(mineCount: Int): Set<Pair<Int, Int>> {
        val minePositions = mutableSetOf<Pair<Int, Int>>()
        while (minePositions.size < mineCount) {
            val x = (0 until xSize).random()
            val y = (0 until ySize).random()
            minePositions.add(x to y)
        }
        return minePositions
    }

    fun cells(): MutableList<String> {
        val grid = mutableListOf<String>()
        for (y in 0 until ySize) {
            val row = StringBuilder()
            for (x in 0 until xSize) {
                row.append(valueAt(x, y))
            }
            grid.add(row.toString())
            println(row)
        }
        return grid
    }

    private fun valueAt(x: Int, y: Int): String {
        if (minePositions.contains(x to y)) return "*"

        val adjacentMineCount = minePositions.filter { adjacent(it, x, y) }.size
        return adjacentMineCount.toString()
    }

    private fun adjacent(location: Pair<Int, Int>, x: Int, y: Int): Boolean {
        return location.first in x - 1..x + 1 &&
                location.second in y - 1..y + 1
    }

    fun modifiers(): MutableList<String> {
        val row = ".".repeat(xSize)
        val rows = (0 until ySize).map { row }.toMutableList()
        // FIXME
        rows[0] = "." + rows[0].substring(1)
        return rows
    }

}
