package org.n1.av2.script.effect.positive.ice

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.sweeper.SweeperIceStatusRepo
import org.n1.av2.layer.ice.sweeper.SweeperService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.IceEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.stereotype.Service


/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.SWEEPER_UNBLOCK
 */
@Service
class SweeperUnblockEffectService(
    private val iceEffectHelper: IceEffectHelper,
    private val sweeperIceStatusRepo: SweeperIceStatusRepo,
    private val currentUserService: CurrentUserService,
    private val sweeperService: SweeperService,
    private val connectionService: ConnectionService,
) : ScriptEffectInterface {


    override val name = "Sweeper unblock"
    override val defaultValue = ""
    override val gmDescription = "Unlock a hacker who clicked on a mine in minesweeper ICE."

    override fun playerDescription(effect: ScriptEffect) = "Unlock a hacker who clicked on a mine in Visphotak ICE (minesweeper)."

    override fun validate(effect: ScriptEffect) = null

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        return iceEffectHelper.runForSpecificIceLayer(LayerType.SWEEPER_ICE, argumentTokens, hackerState) { layer: IceLayer ->
            ScriptExecution {
                val iceStatus = sweeperIceStatusRepo.findByLayerId(layer.id) ?: error("Failed to instantiate ICE for: ${layer.id}")
                sweeperService.unblockHacker(iceStatus, currentUserService.userId)
                connectionService.replyTerminalReceive("Hacker unblocked.")
            }
        }
    }
}
