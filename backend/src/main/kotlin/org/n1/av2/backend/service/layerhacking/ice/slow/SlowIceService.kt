package org.n1.av2.backend.service.layerhacking.ice.slow

import org.n1.av2.backend.engine.SECONDS_IN_TICKS
import org.n1.av2.backend.entity.ice.SlowIceStatus
import org.n1.av2.backend.entity.ice.SlowIceStatusRepo
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.layer.ice.SlowIceLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.HackedUtil
import org.n1.av2.backend.service.layerhacking.ice.slow.SlowIceCreator.Companion.unitsPerSecond
import org.n1.av2.backend.service.user.UserIceHackingService
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class SlowIceService(
    private val slowIceStatusRepo: SlowIceStatusRepo,
    private val stompService: StompService,
    private val hackedUtil: HackedUtil,
    private val userIceHackingService: UserIceHackingService,
) {

    fun createIce(layer: SlowIceLayer, nodeId: String, runId: String): SlowIceStatus {
        val totalUnits = SlowIceCreator().create(layer.strength)
        val id = createId("slowIce", slowIceStatusRepo::findById)

        val slowIceEntity = SlowIceStatus(
            id = id,
            runId = runId,
            nodeId = nodeId,
            layerId = layer.id,
            strength = layer.strength,
            hacked = false,
            totalUnits = totalUnits,
            unitsHacked = 0
        )

        return slowIceStatusRepo.save(slowIceEntity)
    }

    fun deleteAllForRuns(runs: List<Run>) {
        runs.forEach { slowIceStatusRepo.deleteAllByRunId(it.runId) }
    }

    class SlowIceEnter(val iceId: String, val totalUnits: Int, val unitsHacked: Int, val strength: IceStrength, hacked: Boolean, val unitsPerSecond: Int)
    fun enter(iceId: String) {
        val slowIce = slowIceStatusRepo.findById(iceId).getOrElse { error("Netwalk not found for: ${iceId}") }
        if (slowIce.hacked) error("This ice has already been hacked.")

        val playerLevel = 5 // TODO: get actual player level
        val unitsPerSecond = unitsPerSecond(playerLevel)

        stompService.reply(
            ServerActions.SERVER_ENTER_ICE_SLOW,
            SlowIceEnter(iceId, slowIce.totalUnits, slowIce.unitsHacked, slowIce.strength, slowIce.hacked, unitsPerSecond)
        )
        userIceHackingService.enter(iceId)
    }

    fun hackedUnits(iceId: String, units: Int) {
        val slowIce = slowIceStatusRepo.findById(iceId).getOrElse { error("SlowIce not found for: ${iceId}") }
        if (slowIce.hacked) return

        val newUnitsHacked = slowIce.unitsHacked + units
        val hacked = (newUnitsHacked >= slowIce.totalUnits)
        val newStatus  = slowIce.copy(unitsHacked = newUnitsHacked, hacked = hacked)
        slowIceStatusRepo.save(newStatus)

        class SlowIceStatusUpdate(val unitsHacked: Int, val hacked: Boolean)
        val message = SlowIceStatusUpdate(newUnitsHacked, hacked)
        stompService.toIce(iceId, ServerActions.SERVER_SLOW_ICE_UPDATE, message)

        if (hacked) {
            hackedUtil.iceHacked(slowIce.layerId, slowIce.runId, 7 * SECONDS_IN_TICKS)
        }
    }
}
