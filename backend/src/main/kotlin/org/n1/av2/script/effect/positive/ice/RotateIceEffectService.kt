package org.n1.av2.script.effect.positive.ice

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.layer.Layer
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.layer.ice.netwalk.NetwalkIceLayer
import org.n1.av2.layer.ice.sweeper.SweeperIceLayer
import org.n1.av2.layer.ice.tangle.TangleIceLayer
import org.n1.av2.layer.ice.wordsearch.WordSearchIceLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.ThemeService
import org.n1.av2.site.entity.enums.LayerType.*
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.ROTATE_ICE
 */
@Service
class RotateIceEffectService(
    private val scriptEffectHelper: ScriptEffectHelper,
    private val iceService: IceService,
    private val connectionService: ConnectionService,
    private val themeService: ThemeService,
) : ScriptEffectInterface {

    override val name = "Rotate ICE - change ICE type"
    override val defaultValue = null
    override val gmDescription = "Change ICE type: Word search -> Tangle -> Netwalk -> Minesweeper -> Word search"

    override fun playerDescription(effect: ScriptEffect) = gmDescription

    override fun validate(effect: ScriptEffect) = null

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerStateRunning): ScriptExecution {
        val runOnLayerResult = scriptEffectHelper.runOnLayer(argumentTokens, hackerState)
        runOnLayerResult.errorExecution?.let { return it }
        val iceLayer = checkNotNull(runOnLayerResult.layer) as IceLayer
        val node = checkNotNull(runOnLayerResult.node)
        checkIceLayer(iceLayer)?.let { return ScriptExecution(it) }
        return ScriptExecution {
            val rotatedIceLayer = createRotatedIceLayer(iceLayer)

            iceService.changeIce(node, iceLayer, rotatedIceLayer)

            val newName = iceService.formalNameFor(rotatedIceLayer.type)
            connectionService.replyTerminalReceive("ICE type changed to ${newName}.")
        }
    }

    private fun createRotatedIceLayer(layer: IceLayer): IceLayer {
        val newType = when (layer) {
            is WordSearchIceLayer -> TANGLE_ICE
            is TangleIceLayer -> NETWALK_ICE
            is NetwalkIceLayer -> SWEEPER_ICE
            is SweeperIceLayer -> WORD_SEARCH_ICE

            else -> error("Unsupported ice layer: $layer")
        }
        val newName = themeService.themeName(newType)

        val newLayer = iceService.createIceLayer(layer, newType, layer.strength, newName)
        return newLayer
    }

    private fun checkIceLayer(layer: Layer): String? {
        if (layer !is IceLayer) return "This script can only be used on ICE layers."

        if (layer !is WordSearchIceLayer &&
            layer !is TangleIceLayer &&
            layer !is NetwalkIceLayer &&
            layer !is SweeperIceLayer
        ) {
            return "This script cannot be used on this type of ICE."
        }

        if (layer.hacked) return "This ICE layer is already hacked."

        return null
    }

}
