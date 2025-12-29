package org.n1.av2.layer.ice.sweeper

import org.n1.av2.site.entity.enums.IceStrength
import kotlin.random.Random

/**
 * @see SweeperIceStatus for the data model
 */
class SweeperCreator(testingMode: Boolean) {

    private val random = if (testingMode) Random(0L) else Random

    fun createSweeper(iceId: String, layerId: String, iceStrength: IceStrength): SweeperIceStatus {
        (0..20).forEach { _ ->
            val sweeper = createSweeperRaw(iceId, layerId, iceStrength)

            val firstRow = sweeper.cells[0]
            val lastRow = sweeper.cells[sweeper.cells.size - 1]

            // Ensure the top left has a 0, to give players a good place to start.
            if (firstRow.startsWith("0")) return sweeper

            // If top left does not have a 0, check other corners and flip the board accordingly
            if (firstRow.endsWith("0")) return flipX(sweeper)
            if (lastRow.startsWith("0")) return flipY(sweeper)
            if (lastRow.endsWith("0")) return flipY(flipX(sweeper))
        }

        // Give up, just take the next one
        return createSweeperRaw(iceId, layerId, iceStrength)
    }

    private fun flipX(sweeper: SweeperIceStatus): SweeperIceStatus {
        val cells = sweeper.cells.map { it.reversed() }
        return sweeper.copy(cells = cells)
    }

    private fun flipY(sweeper: SweeperIceStatus): SweeperIceStatus {
        val cells = sweeper.cells.reversed()
        return sweeper.copy(cells = cells)
    }

    private fun createSweeperRaw(iceId: String, layerId: String, iceStrength: IceStrength): SweeperIceStatus {
        val map = createMap(iceStrength)
        val cells = map.cells()
        val modifiers = map.modifiers()
        return SweeperIceStatus(iceId, layerId, iceStrength, cells, modifiers, false)
    }

    private fun createMap(iceStrength: IceStrength): SweeperMap {
        return when (iceStrength) {
            IceStrength.VERY_WEAK -> SweeperMap(9, 9, 8, this.random)
            IceStrength.WEAK -> SweeperMap(9, 9, 10, this.random) // Beginner
            IceStrength.AVERAGE -> SweeperMap(16, 16, 40, this.random) // Intermediate
            IceStrength.STRONG -> SweeperMap(22, 16, 60, this.random)
            IceStrength.VERY_STRONG -> SweeperMap(30, 16, 100, this.random) // Expert
            IceStrength.ONYX -> SweeperMap(32, 18, 120, this.random)
        }
    }
}

class SweeperMap(
    private val xSize: Int,
    private val ySize: Int,
    mineCount: Int,
    val random: Random,
) {

    private val minePositions = generateMinePositions(mineCount)

    private fun generateMinePositions(mineCount: Int): Set<Pair<Int, Int>> {
        val minePositions = mutableSetOf<Pair<Int, Int>>()
        while (minePositions.size < mineCount) {
            minePositions.add(newMinePosition())
        }
        return minePositions
    }

    private fun newMinePosition(): Pair<Int, Int> {
        while(true) {
            val x = random.nextInt(xSize)
            val y = random.nextInt(ySize)

            val xAtEdge = (x % (xSize-1) == 0)
            val yAtEdge = (y % (ySize-1) == 0)

            // No mines in the corners
            if (!(xAtEdge && yAtEdge)) {
                return x to y
            }
        }
    }

    fun cells(): MutableList<String> {
        val grid = mutableListOf<String>()
        for (y in 0 until ySize) {
            val row = StringBuilder()
            for (x in 0 until xSize) {
                row.append(valueAt(x, y))
            }
            grid.add(row.toString())
        }
        return grid
    }

    private fun valueAt(x: Int, y: Int): Char {
        if (minePositions.contains(x to y)) return MINE

        val adjacentMineCount = minePositions.filter { adjacent(it, x, y) }.size
        return adjacentMineCount.toString()[0]
    }

    private fun adjacent(location: Pair<Int, Int>, x: Int, y: Int): Boolean {
        return location.first in x - 1..x + 1 &&
                location.second in y - 1..y + 1
    }

    fun modifiers(): MutableList<String> {
        val row = HIDDEN.toString().repeat(xSize)
        val rows = (0 until ySize).map { row }.toMutableList()
        return rows

// Create revealed version, used to determine the graphics to use.

//        val rows = mutableListOf<String>()
//        (0 until ySize).forEach { y ->
//            val row = StringBuilder()
//            (0 until xSize).forEach { x ->
//                if (minePositions.contains(x to y)) {
//                    row.append(FLAG)
//                }
//                else {
//                    row.append(REVEALED)
//                }
//            }
//            rows.add(row.toString())
//        }
//        return rows


    }

}
