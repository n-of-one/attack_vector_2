package org.n1.av2.script.effect.positive.ice

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.tangle.TangleIceStatusRepo
import org.n1.av2.layer.ice.tangle.TangleService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.effect.helper.IceEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.stereotype.Service


/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.TANGLE_REVEAL_CLUSTERS
 */
@Service
class TangleRevealClustersEffectService(
    private val iceEffectHelper: IceEffectHelper,
    private val connectionService: ConnectionService,
    private val tangleIceStatusRepo: TangleIceStatusRepo,
    private val tangleService: TangleService,
) : ScriptEffectInterface {


    override val name = "Tangle reveal clusters"
    override val defaultValue = ""
    override val gmDescription = "Reveal clusters in Tangle ICE."

    override fun playerDescription(effect: ScriptEffect) = "Reveal clusters in Gaanth ICE (tangle)."

    override fun validate(effect: ScriptEffect) = null

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        return iceEffectHelper.runForSpecificIceLayer(LayerType.TANGLE_ICE, argumentTokens, hackerState) { layer: IceLayer ->
            val iceStatus = tangleIceStatusRepo.findByLayerId(layer.id) ?: error("Failed to instantiate ICE for: ${layer.id}")
            tangleService.revealClusters(iceStatus)
            connectionService.replyTerminalReceive("Clusters revealed.")
            TerminalLockState.UNLOCK
        }
    }
}
