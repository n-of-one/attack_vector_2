package org.n1.av2.backend.service.layerhacking.ice.slow

import org.n1.av2.backend.engine.SECONDS_IN_TICKS
import org.n1.av2.backend.entity.ice.SlowIceStatus
import org.n1.av2.backend.entity.ice.SlowIceStatusRepo
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

    fun findOrCreateIceByLayerId(layer: SlowIceLayer): SlowIceStatus {
        return slowIceStatusRepo.findByLayerId(layer.id) ?: createIce(layer)
    }

    private fun createIce(layer: SlowIceLayer): SlowIceStatus {
        val totalUnits = SlowIceCreator().create(layer.strength)
        val id = createId("slowIce", slowIceStatusRepo::findById)

        val slowIceEntity = SlowIceStatus(
            id = id,
            layerId = layer.id,
            strength = layer.strength,
            hacked = false,
            totalUnits = totalUnits,
            unitsHacked = 0
        )

        return slowIceStatusRepo.save(slowIceEntity)
    }

    class SlowIceEnter(val iceId: String, val totalUnits: Int, val unitsHacked: Int, val strength: IceStrength, val hacked: Boolean, val unitsPerSecond: Int)
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
            hackedUtil.iceHacked(slowIce.layerId, 7 * SECONDS_IN_TICKS)
        }
    }

    fun deleteByLayerId(layerId: String) {
        val iceStatus = slowIceStatusRepo.findByLayerId(layerId) ?: return
        slowIceStatusRepo.delete(iceStatus)
    }
}
