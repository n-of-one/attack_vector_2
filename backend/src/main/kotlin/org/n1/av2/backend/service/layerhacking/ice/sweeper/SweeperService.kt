package org.n1.av2.backend.service.layerhacking.ice.sweeper

import org.n1.av2.backend.engine.SECONDS_IN_TICKS
import org.n1.av2.backend.entity.ice.SweeperIceStatus
import org.n1.av2.backend.entity.ice.SweeperIceStatusRepo
import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.layerhacking.HackedUtil
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

enum class SweeperModifyAction { REVEAL, FLAG, QUESTION_MARK, CLEAR, EXPLODE }


@Service
class SweeperService(
    private val stompService: StompService,
    private val hackedUtil: HackedUtil,
    private val sweeperIceStatusRepo: SweeperIceStatusRepo,
    private val currentUser: CurrentUserService,
) {

    private val sweeperCreator = SweeperCreator()

    class SweeperEnter(
        val cells: List<String>,
        val modifiers: List<String>,
        val strength: IceStrength,
        val hacked: Boolean,
        val blockedUserIds: List<String>,
    )

    fun enter(iceId: String, layerId: String, strength: IceStrength) {
//        val iceStatus = sweeperIceStatusRepo.findByLayerId(layerId) ?: sweeperCreator.createSweeper(iceId, layerId, IceStrength.VERY_WEAK)
        val iceStatus = sweeperCreator.createSweeper(iceId, layerId, strength)
        sweeperIceStatusRepo.save(iceStatus)

        val sweeperEnter = SweeperEnter(iceStatus.cells, iceStatus.modifiers, strength, false, iceStatus.blockedUserIds)
        stompService.reply(ServerActions.SERVER_SWEEPER_ENTER, sweeperEnter)
//        runService.enterNetworkedApp(iceId)
    }

    fun interact(iceId: String, x: Int, y: Int, action: SweeperModifyAction) {
        val sweeper: SweeperIceStatus = sweeperIceStatusRepo.findById(iceId).getOrElse { error("Sweeper not found for: ${iceId}") }
        if (sweeper.blockedUserIds.contains(currentUser.userId)) return
        if (sweeper.hacked) return
        if (x < 0 || x >= sweeper.cells[0].length || y < 0 || y >= sweeper.cells.size) error("click out of bounds: $x, $y for iceId: $iceId")
        if (sweeper.modifiers[y][x] == '-') return // cell already revealed by another player, cannot modify.

        if (action == SweeperModifyAction.REVEAL) {
            reveal(sweeper, x, y)
        }
        else {
            modify(sweeper, x, y, action)
        }
    }

    private class SweeperModifyMessage(val cells: List<String>, val action: SweeperModifyAction)

    private fun modify(sweeper: SweeperIceStatus, x: Int, y: Int, action: SweeperModifyAction) {
        val newModifier: String = when (action) {
            SweeperModifyAction.FLAG -> "f"
            SweeperModifyAction.QUESTION_MARK -> "?"
            SweeperModifyAction.CLEAR -> "."
            else -> error("Invalid action: $action")
        }

        saveModification(sweeper, x, y, newModifier)

        stompService.reply(ServerActions.SERVER_SWEEPER_MODIFY, SweeperModifyMessage(listOf("$x:$y"), action))
    }

    private fun saveModification(sweeper: SweeperIceStatus, x: Int, y: Int, newModifier: String) {
        sweeper.modifiers[y] = sweeper.modifiers[y].replaceRange(x, x+1, newModifier)
        sweeperIceStatusRepo.save(sweeper)
    }

    private fun reveal(sweeper: SweeperIceStatus, x: Int, y: Int) {
        when (sweeper.cells[y][x]) {
            '0' -> revealArea(sweeper, x, y)
            '*' -> explode(sweeper, x, y)
            else -> revealSingle(sweeper, x, y)
        }

        if (solved(sweeper)) handleSolved(sweeper)
    }

    class SweeperBlockUserMessage(val userId: String)
    private fun explode(sweeper: SweeperIceStatus, x: Int, y: Int) {
        sweeper.modifiers[y] = sweeper.modifiers[y].replaceRange(x, x+1, "-")
        val sweeperWithUserBlocked = sweeper.copy(blockedUserIds = sweeper.blockedUserIds + currentUser.userId)
        sweeperIceStatusRepo.save(sweeperWithUserBlocked)

        stompService.reply(ServerActions.SERVER_SWEEPER_BLOCK_USER, SweeperBlockUserMessage(currentUser.userId))
        stompService.reply(ServerActions.SERVER_SWEEPER_MODIFY, SweeperModifyMessage(listOf("$x:$y"), SweeperModifyAction.EXPLODE))
    }

    private fun revealSingle(sweeper: SweeperIceStatus, x: Int, y: Int) {
        saveModification(sweeper, x, y, "-")
        stompService.reply(ServerActions.SERVER_SWEEPER_MODIFY, SweeperModifyMessage(listOf("$x:$y"), SweeperModifyAction.REVEAL))
    }

    private fun revealArea(sweeper: SweeperIceStatus, x: Int, y: Int) {
        val here = Pair(x, y)
        val explored = setOf(here).toMutableSet()
        val revealedAround: List<Pair<Int, Int>> = exploreAround(sweeper, x, y, explored)
        val revealed = listOf(here) + revealedAround

        revealed.forEach { (x, y) ->
            sweeper.modifiers[y] = sweeper.modifiers[y].replaceRange(x, x + 1, "-")
        }
        sweeperIceStatusRepo.save(sweeper)

        stompService.reply(ServerActions.SERVER_SWEEPER_MODIFY, SweeperModifyMessage(revealed.map { (x, y) -> "$x:$y"}, SweeperModifyAction.REVEAL))
    }

    private fun exploreAround(sweeper: SweeperIceStatus, x: Int, y: Int, explored: MutableSet<Pair<Int, Int>>): List<Pair<Int, Int>> {
        val revealedCells = (-1..1).flatMap { dx ->
            (-1..1).flatMap { dy ->
                exploreDirection(sweeper, x+dx, y+dy, explored)
            }
        }
        return revealedCells
    }

    private fun exploreDirection(sweeper: SweeperIceStatus, x: Int, y: Int, explored: MutableSet<Pair<Int, Int>>): List<Pair<Int, Int>>{
        if (x < 0 || x >= sweeper.cells[0].length || y < 0 || y >= sweeper.cells.size) return emptyList() // out of bounds
        if (explored.contains(Pair(x, y))) return emptyList() // already explored
        explored.add(Pair(x, y))

        val cell = sweeper.cells[y][x]
        if (cell != '0') return listOf(Pair(x, y)) // not an empty cell, reveal it and stop exploring

        val additionalRevealed = exploreAround(sweeper, x, y, explored)
        return listOf(Pair(x, y)) + additionalRevealed
    }


    class SweeperSolvedMessage(val sweeperId: String)
    private fun handleSolved(sweeper: SweeperIceStatus) {
        val solvedSweeper = sweeper.copy(hacked = true)
        sweeperIceStatusRepo.save(solvedSweeper)
        stompService.reply(ServerActions.SERVER_SWEEPER_SOLVED, SweeperSolvedMessage(sweeper.id))
        hackedUtil.iceHacked(sweeper.layerId, 4 * SECONDS_IN_TICKS)
    }

    private fun solved(sweeper: SweeperIceStatus): Boolean {
        return sweeper.cells
            .zip(sweeper.modifiers) // [([c, m], [c, m], [etc]), ([c, m], [c, m], [etc]), (etc)]
            .flatMap { it.first.zip(it.second) } // [(c, m), (c, m), (c, m), (c, m), (c, m), (etc)]
            .none { unsolvedCell(it) }
    }

    private fun unsolvedCell(input: Pair<Char, Char>): Boolean {
        val (cell, modifier) = input
        return modifier == '.' && cell != '*'
    }
}
