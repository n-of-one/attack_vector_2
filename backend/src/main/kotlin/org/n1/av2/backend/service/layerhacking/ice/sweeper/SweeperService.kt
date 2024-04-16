package org.n1.av2.backend.service.layerhacking.ice.sweeper

import org.n1.av2.backend.entity.ice.SweeperIceStatus
import org.n1.av2.backend.entity.ice.SweeperIceStatusRepo
import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class SweeperService(
    private val stompService: StompService,
    private val runService: RunService,
    private val sweeperIceStatusRepo: SweeperIceStatusRepo,
) {

    private val sweeperCreator = SweeperCreator()

    class SweeperEnter(
        val cells: List<String>,
        val modifiers: List<String>,
        val strength: IceStrength,
        val hacked: Boolean,
    )

    fun enter(iceId: String, layerId: String) {
        val iceStatus = sweeperCreator.createSweeper(iceId, layerId, IceStrength.VERY_WEAK)
        val sweeperEnter = SweeperEnter(iceStatus.cells, iceStatus.modifiers, IceStrength.WEAK, false)
        stompService.reply(ServerActions.SERVER_SWEEPER_ENTER, sweeperEnter)
//        runService.enterNetworkedApp(iceId)
    }

    fun interact(iceId: String, x: Int, y: Int, reveal: Boolean) {
        val sweeper: SweeperIceStatus = sweeperIceStatusRepo.findById(iceId).getOrElse { error("Sweeper not found for: ${iceId}") }
        if (sweeper.hacked) return

        if (x < 0 || x >= sweeper.cells[0].length || y < 0 || y >= sweeper.cells.size) error("click out of bounds: $x, $y for iceId: $iceId")

        val modifier = sweeper.modifiers[y][x]

        if (modifier == '-' ) return // cell already revealed
        if (reveal) {
            reveal(sweeper, x, y)
        }
        else {
            modify(sweeper, x, y, )
        }
    }

    private fun modify(sweeper: SweeperIceStatus, x: Int, y: Int) {
        val newModifier = when (val modifier = sweeper.modifiers[y][x]) {
            '.' -> "f"
            'f' -> "?"
            '?' -> "."
            else -> error("Invalid modifier: $modifier")
        }

        sweeper.modifiers[y] = sweeper.modifiers[y].replaceRange(x, x+1, newModifier)

        sweeperIceStatusRepo.save(sweeper)
        stompService.reply(ServerActions.SERVER_SWEEPER_MODIFY, Pair(x, y))

    }
}