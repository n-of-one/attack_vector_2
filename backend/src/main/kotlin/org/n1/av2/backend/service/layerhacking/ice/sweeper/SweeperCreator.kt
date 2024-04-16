package org.n1.av2.backend.service.layerhacking.ice.sweeper

import org.n1.av2.backend.entity.ice.SweeperIceStatus
import org.n1.av2.backend.entity.site.enums.IceStrength

class SweeperStats (
    val xSize: Int,
    val ySize: Int,
    val mines: Int,
)



class SweeperCreator {

     fun createSweeper(iceId: String, layerId: String, iceStrength: IceStrength): SweeperIceStatus {
        val stats = statesByIceStrength(iceStrength)
        val cells = createCells(stats.xSize, stats.ySize, stats.mines)
        val modifiers = createModifiers(stats.xSize, stats.ySize)
         return SweeperIceStatus(iceId, layerId, iceStrength, cells, modifiers, false)
     }

    fun statesByIceStrength(iceStrength: IceStrength): SweeperStats {
        return when (iceStrength) {
            IceStrength.VERY_WEAK -> SweeperStats(9, 9, 10)
            IceStrength.WEAK -> SweeperStats(16, 16, 40)
            IceStrength.AVERAGE -> SweeperStats(30, 16, 99)
            IceStrength.STRONG -> SweeperStats(24, 24, 99)
            IceStrength.VERY_STRONG -> SweeperStats(24, 24, 99)
            IceStrength.ONYX -> SweeperStats(24, 24, 99)
        }
    }

    private fun createCells(xSize: Int, ySize: Int, mineCount: Int): List<String> {
        val minePositions = generateMinePositions(xSize, ySize, mineCount)
        val grid = mutableListOf<String>()
        for (y in 0 until ySize) {
            val row = StringBuilder()
            for (x in 0 until xSize) {
                row.append(if (minePositions.contains(x to y)) "*" else ".")
            }
            grid.add(row.toString())
            println(row)
        }
        return grid
    }

    private fun generateMinePositions(xSize: Int, ySize: Int, totalMines: Int): Set<Pair<Int, Int>> {
        val minePositions = mutableSetOf<Pair<Int, Int>>()
        while (minePositions.size < totalMines) {
            val x = (0 until xSize).random()
            val y = (0 until ySize).random()
            minePositions.add(x to y)
        }
        return minePositions
    }

    private fun createModifiers(xSize: Int, ySize: Int): List<String> {
        val modifiers = mutableListOf<String>()
        for (y in 0 until ySize) {
            val row = StringBuilder()
            for (x in 0 until xSize) {
                row.append(".")
            }
            modifiers.add(row.toString())
        }
        return modifiers
    }

}
