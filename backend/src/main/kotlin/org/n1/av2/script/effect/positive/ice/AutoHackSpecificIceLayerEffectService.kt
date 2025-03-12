package org.n1.av2.script.effect.positive.ice

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.IceEffectHelper
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.effect.scriptCannotInteractWithThisLayer
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.AUTO_HACK_SPECIFIC_ICE_LAYER
 */
@Service
class AutoHackSpecificIceLayerEffectService(
    private val iceEffectHelper: IceEffectHelper,
    private val nodeEffectService: NodeEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val scriptEffectHelper: ScriptEffectHelper,

) : ScriptEffectInterface {

    override val name = "Automatically hack a specific layer of ICE"
    override val defaultValue = "node-1234-5678:layer-1234"
    override val gmDescription = "Automatically hack one specific layer of ICE in a specific site."

    override fun playerDescription(effect: ScriptEffect): String {
        return createPlayerDescription(effect) ?: "Script functionality is broken. Unable to execute script."
    }

    private fun createPlayerDescription(effect: ScriptEffect): String? {
        try {
            val node = nodeEffectService.findByLayerId(effect.value!!)
            val site = sitePropertiesEntityService.getBySiteId(node.siteId)
            val layer = node.getLayerById(effect.value)
            if (layer !is IceLayer) error("non-ice layer, cannot autohack")
            val nodeNetworkId = node.networkId

            return "Automatically hack layer ${layer.level} of node: ${nodeNetworkId} of site: ${site.name}."
        }
        catch (_: Exception) {
            return null
        }
    }

    override fun validate(effect: ScriptEffect): String? {
        createPlayerDescription(effect)?.let { return null }
        return "layer id not found. Please copy/paste the layer ID of a site. It should look like: node-1234-5678:layer-1234 ."
    }

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        val runOnLayerResult = scriptEffectHelper.runOnLayer(argumentTokens, hackerState)
        runOnLayerResult.errorExecution?.let { return it }

        val layer = checkNotNull(runOnLayerResult.layer)
        if (layer.id != effect.value || layer !is IceLayer) {
            return ScriptExecution(scriptCannotInteractWithThisLayer)
        }

        return iceEffectHelper.autoHack(layer, hackerState)
    }
}
