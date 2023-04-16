package org.n1.av2.backend.service.layerhacking.netwalk

import org.n1.av2.backend.entity.ice.*
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
    fun createIce(layer: IceNetwalkLayer, nodeId: String, runId: String): NetwalkEntity {
        val creator = NetwalkCreator(layer.strength)
        val grid: List<List<NetwalkCellMinimal>> = creator.create()
        val id = createId("netwalk", netwalkStatusRepo::findById)

        val netwalkEntity = NetwalkEntity(
            id = id,
            runId = runId,
            nodeId = nodeId,
            layerId = layer.id,
            strength = layer.strength,
            cellGrid = grid,
            hacked = false,
        )
        return netwalkStatusRepo.save(netwalkEntity)
    }


    class NetwalkEnter(val iceId: String,val cellGrid: List<List<NetwalkCellMinimal>>,val strength: IceStrength, hacked: Boolean)
    fun enter(iceId: String) {
        val netwalk = netwalkStatusRepo.findById(iceId).getOrElse { error("Netwalk not found for: ${iceId}") }
        stompService.reply(ServerActions.SERVER_ENTER_ICE_NETWALK, NetwalkEnter(iceId, netwalk.cellGrid, netwalk.strength, netwalk.hacked))
    }

    fun rotate(iceId: String, x: Int, y: Int) {
        val netwalk = netwalkStatusRepo.findById(iceId).getOrElse { error("Netwalk not found for: ${iceId}") }
        if (netwalk.hacked) return

        val gridWithRotatedCell = rotateRight(x, y, netwalk.cellGrid)
        val (connectedList, gridWithConnections) = NetwalkConnectionTracer(gridWithRotatedCell).trace()

        val flatGrid = gridWithConnections.flatMap { it }
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
            hackedUtil.iceHacked(netwalk.layerId, netwalk.runId, 70)
        }
    }

    private fun rotateRight(x: Int, y: Int, cellGrid: List<List<NetwalkCellMinimal>>): List<List<NetwalkCellMinimal>> {
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