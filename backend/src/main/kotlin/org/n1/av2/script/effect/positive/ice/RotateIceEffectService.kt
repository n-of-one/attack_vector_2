package org.n1.av2.script.effect.positive.ice

import org.n1.av2.hacker.hackerstate.HackerState
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
import org.n1.av2.site.entity.NodeEntityService
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
    private val nodeEntityService: NodeEntityService,
    private val themeService: ThemeService,
) : ScriptEffectInterface {

    override val name = "Rotate ICE - change ICE type"
    override val defaultValue = null
    override val gmDescription = "Change ICE type: Word search -> Tangle -> Netwalk -> Minesweeper -> Word search"

    override fun playerDescription(effect: ScriptEffect) = gmDescription

    override fun validate(effect: ScriptEffect) = null

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        val runOnLayerResult = scriptEffectHelper.runOnLayer(argumentTokens, hackerState)
        runOnLayerResult.errorExecution?.let { return it }
        val iceLayer = checkNotNull(runOnLayerResult.layer) as IceLayer
        val node = checkNotNull(runOnLayerResult.node)
        checkIceLayer(iceLayer)?.let { return ScriptExecution(it) }
        return ScriptExecution {

            iceService.resetIceForLayer(iceLayer)
            val rotatedIceLayer = rotateIceLayer(iceLayer)
            node.layers[node.layers.indexOf(iceLayer)] = rotatedIceLayer
            nodeEntityService.save(node)
            iceService.findOrCreateIceForLayerAndIceStatus(rotatedIceLayer)
            val newName = iceService.formalNameFor(rotatedIceLayer.type)
            iceService.informFrontendOfLayerChanged(node, rotatedIceLayer)
            connectionService.replyTerminalReceive("ICE type changed to ${newName}.")
        }
    }


    private fun rotateIceLayer(layer: IceLayer): IceLayer {
        val originalLayer = layer.original ?: layer

        return when (layer) {
            is WordSearchIceLayer -> TangleIceLayer(
                layer.id, TANGLE_ICE, layer.level, themeService.themeName(TANGLE_ICE),
                layer.note, layer.strength, false, 2, originalLayer
            )

            is TangleIceLayer -> NetwalkIceLayer(
                layer.id, NETWALK_ICE, layer.level, themeService.themeName(NETWALK_ICE),
                layer.note, layer.strength, false, originalLayer
            )

            is NetwalkIceLayer -> SweeperIceLayer(
                layer.id, SWEEPER_ICE, layer.level, themeService.themeName(SWEEPER_ICE),
                layer.note, layer.strength, false, originalLayer
            )

            is SweeperIceLayer -> WordSearchIceLayer(
                layer.id, WORD_SEARCH_ICE, layer.level, themeService.themeName(WORD_SEARCH_ICE),
                layer.note, layer.strength, false, originalLayer
            )

            else -> error("Unsupported ice layer: $layer")
        }
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
