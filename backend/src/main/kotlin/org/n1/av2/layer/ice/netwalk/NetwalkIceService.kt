package org.n1.av2.layer.ice.netwalk

import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.SECONDS_IN_TICKS
import org.n1.av2.platform.util.createId
import org.n1.av2.run.RunService
import org.n1.av2.site.entity.enums.IceStrength
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class NetwalkIceService(
    private val connectionService: ConnectionService,
    private val netwalkIceStatusRepo: NetwalkIceStatusRepo,
    private val hackedUtil: HackedUtil,
    private val runService: RunService,
    private val configService: ConfigService,
    ) {

    companion object {
        const val MAX_CREATE_ATTEMPTS = 20
    }

    fun findOrCreateIceByLayerId(layer: NetwalkIceLayer): NetwalkIceStatus {
        return netwalkIceStatusRepo.findByLayerId(layer.id) ?: createIce(layer)
    }

    private fun createIce(layer: NetwalkIceLayer): NetwalkIceStatus {
        val puzzle = createNetwalkPuzzle(layer.strength)
        val id = createId("netwalk", netwalkIceStatusRepo::findById)

        val netwalkIceStatus = NetwalkIceStatus(
            id = id,
            layerId = layer.id,
            strength = layer.strength,
            cellGrid = puzzle.grid,
            hacked = false,
            wrapping = puzzle.wrapping
        )
        return netwalkIceStatusRepo.save(netwalkIceStatus)
    }

    fun createNetwalkPuzzle(iceStrength: IceStrength): NetwalkPuzzle {
        repeat(MAX_CREATE_ATTEMPTS) {
            val creator = NetwalkCreator(iceStrength)
            val puzzle = creator.create()
            if (puzzle != null) return puzzle
        }
        error("Failed to create netwalk puzzle after $MAX_CREATE_ATTEMPTS attempts")
    }


    @Suppress("unused")
    class NetwalkEnter(val iceId: String, val cellGrid: List<List<NetwalkCell>>, val strength: IceStrength, val wrapping: Boolean, val quickPlaying: Boolean)
    fun enter(iceId: String) {
        val netwalk = netwalkIceStatusRepo.findById(iceId).getOrElse { error("Netwalk not found for: ${iceId}") }
        val quickPlaying = configService.getAsBoolean(ConfigItem.DEV_QUICK_PLAYING)
        connectionService.reply(ServerActions.SERVER_NETWALK_ENTER, NetwalkEnter(iceId, netwalk.cellGrid, netwalk.strength, netwalk.wrapping, quickPlaying))
        runService.enterNetworkedApp(iceId)
    }

    fun rotate(iceId: String, x: Int, y: Int) {
        val netwalk = netwalkIceStatusRepo.findById(iceId).getOrElse { error("Netwalk not found for: ${iceId}") }
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

        netwalkIceStatusRepo.save(updatedNetWalk)

        @Suppress("unused")
        class NetwalkRotateUpdate(val iceId: String, val x: Int, val y: Int, val connected: List<Point>)
        val message = NetwalkRotateUpdate(iceId, x, y, connectedList)
        connectionService.toIce(iceId, ServerActions.SERVER_NETWALK_NODE_ROTATED, message)

        if (hacked) {
            hackedUtil.iceHacked(iceId, netwalk.layerId, 7 * SECONDS_IN_TICKS)
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

    fun deleteByLayerId(layerId: String) {
        val iceStatus = netwalkIceStatusRepo.findByLayerId(layerId) ?: return
        netwalkIceStatusRepo.delete(iceStatus)
    }
}
