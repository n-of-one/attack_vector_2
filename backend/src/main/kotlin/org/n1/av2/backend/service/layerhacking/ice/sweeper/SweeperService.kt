package org.n1.av2.backend.service.layerhacking.ice.sweeper

import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.engine.SECONDS_IN_TICKS
import org.n1.av2.backend.entity.ice.*
import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.ice.SweeperIceLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.layerhacking.HackedUtil
import org.n1.av2.backend.service.layerhacking.app.auth.AuthAppService
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

enum class SweeperModifyAction { REVEAL, FLAG, CLEAR, EXPLODE }

/**
 * @see SweeperIceStatus for the data model
 */
@Service
class SweeperService(
    private val stompService: StompService,
    private val hackedUtil: HackedUtil,
    private val sweeperIceStatusRepo: SweeperIceStatusRepo,
    private val currentUser: CurrentUserService,
    private val authAppService: AuthAppService,
    private val serverConfig: ServerConfig,
    private val runService: RunService,
) {

    private val sweeperCreator = SweeperCreator()

    class SweeperEnter(
        val cells: List<String>,
        val modifiers: List<String>,
        val strength: IceStrength,
        val hacked: Boolean,
        val blockedUserIds: List<String>,
        val minesLeft: Int
    )


    fun findOrCreateIceStatus(layer: SweeperIceLayer): SweeperIceStatus {
        return sweeperIceStatusRepo.findByLayerId(layer.id) ?: createIce(layer.id, layer.strength)
    }

    fun createIce(layerId: String, strength: IceStrength): SweeperIceStatus {
        val id = createId("sweeper", sweeperIceStatusRepo::findById)
        val iceStatus = sweeperCreator.createSweeper(id, layerId, strength)

        sweeperIceStatusRepo.save(iceStatus)
        return iceStatus
    }

    fun enter(iceId: String) {
        val sweeper = sweeperIceStatusRepo.findById(iceId).getOrElse { error("Sweeper not found for: ${iceId}") }

        val minesLeft = minesLeft(sweeper)

        val sweeperEnter = SweeperEnter(sweeper.cells, sweeper.modifiers, sweeper.strength, false, sweeper.blockedUserIds, minesLeft)
        stompService.reply(ServerActions.SERVER_SWEEPER_ENTER, sweeperEnter)
        runService.enterNetworkedApp(iceId)
    }

    private fun minesLeft(sweeper: SweeperIceStatus): Int {
        var minesMarked = 0
        (0 until sweeper.cells.size).forEach{ y ->
            (0 until sweeper.cells[0].length).forEach { x ->
                val modifier = sweeper.modifiers[y][x]
                val cell = sweeper.cells[y][x]
                if (modifier == FLAG || (modifier == REVEALED && cell == MINE )) minesMarked++
            }
        }
        val minesTotal  = sweeper.cells.sumOf { row -> row.count { cell -> cell == MINE } }
        return minesTotal - minesMarked
    }

    fun interact(iceId: String, x: Int, y: Int, action: SweeperModifyAction) {
        val sweeper: SweeperIceStatus = sweeperIceStatusRepo.findById(iceId).getOrElse { error("Sweeper not found for: ${iceId}") }
        if (sweeper.blockedUserIds.contains(currentUser.userId)) return
        if (sweeper.hacked) return
        if (x < 0 || x >= sweeper.cells[0].length || y < 0 || y >= sweeper.cells.size) error("click out of bounds: $x, $y for iceId: $iceId")
        if (sweeper.modifiers[y][x] == REVEALED) return // cell already revealed by another player, cannot modify.

        if (action == SweeperModifyAction.REVEAL) {
            reveal(sweeper, x, y)
        }
        else {
            modify(sweeper, x, y, action)
        }

        if (isCompleted(sweeper)) handleSolved(sweeper)
    }

    private class SweeperModifyMessage(val cells: List<String>, val action: SweeperModifyAction)

    private fun modify(sweeper: SweeperIceStatus, x: Int, y: Int, action: SweeperModifyAction) {
        val newModifier: Char = when (action) {
            SweeperModifyAction.FLAG -> FLAG
            SweeperModifyAction.CLEAR -> HIDDEN
            else -> error("Invalid action: $action")
        }

        saveModification(sweeper, x, y, newModifier)

        stompService.toIce(sweeper.id, ServerActions.SERVER_SWEEPER_MODIFY, SweeperModifyMessage(listOf("$x:$y"), action))
    }

    private fun saveModification(sweeper: SweeperIceStatus, x: Int, y: Int, newModifier: Char) {
        sweeper.modifiers[y] = sweeper.modifiers[y].replaceRange(x, x+1, newModifier.toString())
        sweeperIceStatusRepo.save(sweeper)
    }

    // All mines flagged or revealed, all non-mines revealed.
    private fun isCompleted(sweeper: SweeperIceStatus): Boolean {
        (0 until sweeper.cells.size).forEach{ y ->
            (0 until sweeper.cells[0].length).forEach { x ->
                val modifier = sweeper.modifiers[y][x]
                val cell = sweeper.cells[y][x]
                if (cell == MINE && modifier != REVEALED && modifier != FLAG) {
                    return false
                }
                if (cell != MINE && modifier != REVEALED) {
                    return false
                }
            }
        }
        return true
    }

    private fun reveal(sweeper: SweeperIceStatus, x: Int, y: Int) {
        if (sweeper.modifiers[y][x] != HIDDEN) return // can only reveal hidden cells
        when (sweeper.cells[y][x]) {
            NEIGHBOUR_MINES_0 -> revealArea(sweeper, x, y)
            MINE -> explode(sweeper, x, y)
            else -> revealSingle(sweeper, x, y)
        }

//        In competitive Minesweeper, the game is completed when all non-mine cells are revealed.
//        This is not intuitive for new players, to we'll leave it out.
//        if (areAllNonMinesRevealed(sweeper)) handleSolved(sweeper)
    }

    class SweeperBlockUserMessage(val userId: String, val userName: String)
    private fun explode(sweeper: SweeperIceStatus, x: Int, y: Int) {
        sweeper.modifiers[y] = sweeper.modifiers[y].replaceRange(x, x+1, REVEALED.toString())
        val sweeperWithUserBlocked = sweeper.copy(blockedUserIds = sweeper.blockedUserIds + currentUser.userId)
        sweeperIceStatusRepo.save(sweeperWithUserBlocked)

        stompService.toIce(sweeper.id, ServerActions.SERVER_SWEEPER_BLOCK_USER, SweeperBlockUserMessage(currentUser.userId, currentUser.userEntity.name))
        stompService.toIce(sweeper.id, ServerActions.SERVER_SWEEPER_MODIFY, SweeperModifyMessage(listOf("$x:$y"), SweeperModifyAction.EXPLODE))
    }

    private fun revealSingle(sweeper: SweeperIceStatus, x: Int, y: Int) {
        saveModification(sweeper, x, y, REVEALED)
        stompService.toIce(sweeper.id, ServerActions.SERVER_SWEEPER_MODIFY, SweeperModifyMessage(listOf("$x:$y"), SweeperModifyAction.REVEAL))
    }

    private fun revealArea(sweeper: SweeperIceStatus, x: Int, y: Int) {
        val here = Pair(x, y)
        val explored = setOf(here).toMutableSet()
        val revealedAround: List<Pair<Int, Int>> = exploreAround(sweeper, x, y, explored)
        val revealed = listOf(here) + revealedAround

        revealed.forEach { (x, y) ->
            sweeper.modifiers[y] = sweeper.modifiers[y].replaceRange(x, x + 1, REVEALED.toString())
        }
        sweeperIceStatusRepo.save(sweeper)

        stompService.toIce(sweeper.id, ServerActions.SERVER_SWEEPER_MODIFY, SweeperModifyMessage(revealed.map { (x, y) -> "$x:$y"}, SweeperModifyAction.REVEAL))
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
        if (sweeper.modifiers[y][x] == FLAG) return emptyList() // flagged cell
        if (explored.contains(Pair(x, y))) return emptyList() // already explored
        explored.add(Pair(x, y))

        val cell = sweeper.cells[y][x]
        if (cell != NEIGHBOUR_MINES_0) return listOf(Pair(x, y)) // not an empty cell, reveal it and stop exploring

        val additionalRevealed = exploreAround(sweeper, x, y, explored)
        return listOf(Pair(x, y)) + additionalRevealed
    }


    class SweeperSolvedMessage(val sweeperId: String)
    private fun handleSolved(sweeper: SweeperIceStatus) {
        val solvedSweeper = sweeper.copy(hacked = true)
        sweeperIceStatusRepo.save(solvedSweeper)
        stompService.toIce(sweeper.id, ServerActions.SERVER_SWEEPER_SOLVED, SweeperSolvedMessage(sweeper.id))
        hackedUtil.iceHacked(sweeper.layerId, 4 * SECONDS_IN_TICKS)
    }

    private fun areAllNonMinesRevealed(sweeper: SweeperIceStatus): Boolean {
        return sweeper.cells
            .zip(sweeper.modifiers) // [([c, m], [c, m], [etc]), ([c, m], [c, m], [etc]), (etc)]
            .flatMap { it.first.zip(it.second) } // [(c, m), (c, m), (c, m), (c, m), (c, m), (etc)]
            .none { unsolvedCell(it) }
    }

    private fun unsolvedCell(input: Pair<Char, Char>): Boolean {
        val (cell, modifier) = input
        return modifier == HIDDEN && cell != MINE // cell is hidden and is not a mine
    }

    class SweeperResetMessage(val userName: String)
    fun startReset(iceId: String) {
        val sweeper = sweeperIceStatusRepo.findById(iceId).getOrElse {
            return // sweeper not found, probably reset already complete
        }
        if (sweeper.hacked) return // cannot reset a solved sweeper
        stompService.toIce(iceId, ServerActions.SERVER_SWEEPER_RESET_START, SweeperResetMessage(currentUser.userEntity.name))
    }

    fun stopReset(iceId: String) {
        val sweeper = sweeperIceStatusRepo.findById(iceId).getOrElse {
            return // sweeper not found, probably reset already complete
        }
        if (sweeper.hacked) return // cannot reset a solved sweeper
        stompService.toIce(iceId, ServerActions.SERVER_SWEEPER_RESET_STOP, SweeperResetMessage(currentUser.userEntity.name))
    }

    class SweeperResetCompleteMessage(val userName: String, newIceId: String)
    fun completeReset(iceId: String) {
        val sweeper = sweeperIceStatusRepo.findById(iceId).getOrElse {
            return // sweeper not found, probably reset already complete
        }
        if (sweeper.hacked) return // cannot reset a solved sweeper

        sweeperIceStatusRepo.delete(sweeper)
        authAppService.deleteByLayerId(sweeper.layerId)
        val newSweeper = createIce(sweeper.layerId, sweeper.strength)

        stompService.toIce(iceId, ServerActions.SERVER_SWEEPER_RESET_COMPLETE, SweeperResetCompleteMessage(currentUser.userEntity.name, newSweeper.id))
    }

    fun deleteByLayerId(layerId: String) {
        val iceStatus = sweeperIceStatusRepo.findByLayerId(layerId) ?: return
        sweeperIceStatusRepo.delete(iceStatus)
    }

    // DEV debug: unblock player
    fun debugUnblock(layer: SweeperIceLayer, userId: String) {
        if (!serverConfig.dev) return stompService.replyTerminalReceive("Debug unblock only available in dev mode.")
        val iceStatus = sweeperIceStatusRepo.findByLayerId(layer.id) ?: return stompService.replyTerminalReceive("Ice not found for ${layer.id}.")
        val updatedBlockedUserIds = iceStatus.blockedUserIds - userId
        val updatedIceStatus = iceStatus.copy(blockedUserIds = updatedBlockedUserIds)
        sweeperIceStatusRepo.save(updatedIceStatus)
        stompService.replyTerminalReceive("Unblocked current player for ICE.")
    }


}
