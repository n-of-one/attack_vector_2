package org.n1.av2.backend.service.layerhacking.netwalk

import org.n1.av2.backend.engine.SECONDS_IN_TICKS
import org.n1.av2.backend.entity.ice.*
import org.n1.av2.backend.entity.ice.NetwalkCell
import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.layer.IceNetwalkLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.HackedUtil
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class IceNetwalkService(
    private val stompService: StompService,
    private val netwalkStatusRepo: NetwalkStatusRepo,
    val hackedUtil: HackedUtil,
) {

    companion object {
        const val MAX_CREATE_ATTEMPTS = 20
    }

    fun createIce(layer: IceNetwalkLayer, nodeId: String, runId: String): NetwalkEntity {
        val puzzle = createNetwalkPuzzle(layer.strength)
        val id = createId("netwalk", netwalkStatusRepo::findById)

        val netwalkEntity = NetwalkEntity(
            id = id,
            runId = runId,
            nodeId = nodeId,
            layerId = layer.id,
            strength = layer.strength,
            cellGrid = puzzle.grid,
            hacked = false,
            wrapping = puzzle.wrapping
        )
        return netwalkStatusRepo.save(netwalkEntity)
    }

    fun createNetwalkPuzzle(iceStrength: IceStrength): NetwalkPuzzle {
        repeat(MAX_CREATE_ATTEMPTS) {
            val creator = NetwalkCreator(iceStrength)
            val puzzle = creator.create()
            if (puzzle != null) return puzzle
        }
        error("Failed to create netwalk puzzle after ${MAX_CREATE_ATTEMPTS} attempts")
    }


    class NetwalkEnter(val iceId: String, val cellGrid: List<List<NetwalkCell>>, val strength: IceStrength, hacked: Boolean, val wrapping: Boolean)
    fun enter(iceId: String) {
        val netwalk = netwalkStatusRepo.findById(iceId).getOrElse { error("Netwalk not found for: ${iceId}") }
        if (netwalk.hacked) error("This ice has already been hacked.")
        stompService.reply(ServerActions.SERVER_ENTER_ICE_NETWALK, NetwalkEnter(iceId, netwalk.cellGrid, netwalk.strength, netwalk.hacked, netwalk.wrapping))
    }

    fun rotate(iceId: String, x: Int, y: Int) {
        val netwalk = netwalkStatusRepo.findById(iceId).getOrElse { error("Netwalk not found for: ${iceId}") }
        if (netwalk.hacked) return

        val gridWithRotatedCell = rotateRight(x, y, netwalk.cellGrid)
        val (connectedList, gridWithConnections) =
            NetwalkConnectionTracer(gridWithRotatedCell, netwalk.wrapping).trace()

        val flatGrid = gridWithConnections.flatten()
        val totalConnected = flatGrid.count { it.connected }
        val hacked = (totalConnected == flatGrid.size)

        val updatedNetWalk = netwalk.copy(
            cellGrid = gridWithConnections,
            hacked = hacked
        )

        netwalkStatusRepo.save(updatedNetWalk)

        class NetwalkRotateUpdate(val iceId: String, val x: Int, val y: Int, val connected: List<Point>, val hacked: Boolean)
        val message = NetwalkRotateUpdate(iceId, x, y, connectedList, hacked)
        stompService.toIce(iceId, ServerActions.SERVER_NETWALK_NODE_ROTATED, message)

        if (hacked) {
            hackedUtil.iceHacked(netwalk.layerId, netwalk.runId, 7 * SECONDS_IN_TICKS)
        }
    }

    private fun rotateRight(x: Int, y: Int, cellGrid: List<List<NetwalkCell>>): List<List<NetwalkCell>> {
        val rotatedCellType = cellGrid[y][x].type.rotateClockwise()

        val gridWithRotatedCell = cellGrid.mapIndexed { yIndex, row ->
            row.mapIndexed { xIndex, cell ->
                if (xIndex == x && yIndex == y) {
                    cell.copy(type = rotatedCellType)
                } else {
                    cell
                }
            }
        }
        return gridWithRotatedCell
    }
}
