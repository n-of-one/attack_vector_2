package org.n1.av2.layer.ice.tar

import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.layer.ice.tar.TarCreator.Companion.unitsPerSecond
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.SECONDS_IN_TICKS
import org.n1.av2.platform.util.createId
import org.n1.av2.run.RunService
import org.n1.av2.site.entity.enums.IceStrength
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class TarService(
    private val tarIceStatusRepo: TarIceStatusRepo,
    private val connectionService: ConnectionService,
    private val hackedUtil: HackedUtil,
    private val runService: RunService,
) {

    fun findOrCreateIceByLayerId(layer: TarIceLayer): TarIceStatus {
        return tarIceStatusRepo.findByLayerId(layer.id) ?: createIce(layer)
    }

    private fun createIce(layer: TarIceLayer): TarIceStatus {
        val totalUnits = TarCreator().create(layer.strength)
        val id = createId("tar", tarIceStatusRepo::findById)

        val tarIceStatus = TarIceStatus(
            id = id,
            layerId = layer.id,
            strength = layer.strength,
            hacked = false,
            totalUnits = totalUnits,
            unitsHacked = 0
        )

        return tarIceStatusRepo.save(tarIceStatus)
    }

    class TarEnter(val iceId: String, val totalUnits: Int, val unitsHacked: Int, val strength: IceStrength, val hacked: Boolean, val unitsPerSecond: Int)
    fun enter(iceId: String) {
        val tarIceStatus = tarIceStatusRepo.findById(iceId).getOrElse { error("Netwalk not found for: ${iceId}") }

        val playerLevel = 5 // TODO: get actual player level
        val unitsPerSecond = unitsPerSecond(playerLevel)

        connectionService.reply(
            ServerActions.SERVER_TAR_ENTER,
            TarEnter(iceId, tarIceStatus.totalUnits, tarIceStatus.unitsHacked, tarIceStatus.strength, tarIceStatus.hacked, unitsPerSecond)
        )
        runService.enterNetworkedApp(iceId)
    }

    fun hackedUnits(iceId: String, units: Int) {
        val tarIceStatus = tarIceStatusRepo.findById(iceId).getOrElse { error("Tar not found for: ${iceId}") }
        if (tarIceStatus.hacked) return

        val newUnitsHacked = tarIceStatus.unitsHacked + units
        val hacked = (newUnitsHacked >= tarIceStatus.totalUnits)
        val newStatus  = tarIceStatus.copy(unitsHacked = newUnitsHacked, hacked = hacked)
        tarIceStatusRepo.save(newStatus)

        class TarStatusUpdate(val unitsHacked: Int, val hacked: Boolean)
        val message = TarStatusUpdate(newUnitsHacked, hacked)
        connectionService.toIce(iceId, ServerActions.SERVER_TAR_UPDATE, message)

        if (hacked) {
            hackedUtil.iceHacked(tarIceStatus.layerId, 7 * SECONDS_IN_TICKS)
        }
    }

    fun deleteByLayerId(layerId: String) {
        val tarIceStatus = tarIceStatusRepo.findByLayerId(layerId) ?: return
        tarIceStatusRepo.delete(tarIceStatus)
    }
}
