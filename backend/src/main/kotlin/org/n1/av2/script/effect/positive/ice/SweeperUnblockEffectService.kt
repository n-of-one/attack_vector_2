package org.n1.av2.script.effect.positive.ice

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.ice.sweeper.SweeperIceLayer
import org.n1.av2.layer.ice.sweeper.SweeperIceStatusRepo
import org.n1.av2.layer.ice.sweeper.SweeperService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service


/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.SWEEPER_UNBLOCK
 */
@Service
class SweeperUnblockEffectService(
    private val scriptEffectHelper: ScriptEffectHelper,
    private val nodeEntityService: NodeEntityService,
    private val sweeperIceStatusRepo: SweeperIceStatusRepo,
    private val currentUserService: CurrentUserService,
    private val sweeperService: SweeperService,
    private val connectionService: ConnectionService,
    ) : ScriptEffectInterface {


    override val name = "Sweeper respawn"
    override val defaultValue = ""
    override val gmDescription = "Unlock a player who clicked on a mine in minesweeper ICE."

    override fun playerDescription(effect: ScriptEffect) = "Unlock the player who clicked on a mine in Visphotak ICE (minesweeper)."

    override fun validate(effect: ScriptEffect) = null

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        scriptEffectHelper.checkInNode(hackerState)?.let { return ScriptExecution(it) }
        val node = nodeEntityService.getById(hackerState.currentNodeId!!)
        val sweeperLayers = node.layers
            .filterIsInstance<SweeperIceLayer>()
            .filter { !it.hacked }

        if (sweeperLayers.isEmpty()) {
            return ScriptExecution("This node has no unhacked Visphotak ICE.")
        }

        val sweeperLayer = sweeperLayers
            .sortedBy { it.level }
            .reversed() // highest level first
            .find { layer ->
            val iceStatus = sweeperIceStatusRepo.findByLayerId(layer.id)
            iceStatus != null && iceStatus.blockedUserIds.contains(currentUserService.userId)
        }

        if (sweeperLayer == null) return ScriptExecution("You are not blocked by the ICE")
        val iceStatus = sweeperIceStatusRepo.findByLayerId(sweeperLayer.id)?: error("ICE status changed during call")

        return ScriptExecution {
            sweeperService.unblockHacker(iceStatus, currentUserService.userId)
            connectionService.replyTerminalReceive("Unblocked ICE.")
            TerminalLockState.UNLOCK
        }
    }
}
