package org.n1.av2.script.effect.helper

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.stereotype.Service

@Service
class ScriptEffectHelper(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val iceService: IceService,
) {

    fun checkAtNonShutdownSite(hackerState: HackerState): String? {
        if (hackerState.activity == HackerActivity.OFFLINE || hackerState.siteId == null) {
            return "You must run this script when hacking a site."
        }

        val siteState = sitePropertiesEntityService.getBySiteId(hackerState.siteId)
        if (siteState.shutdownEnd != null) {
            return "Cannot run this script while the site is shut down."
        }
        return null
    }

    fun checkInRun(state: HackerState): String? {
        if (state.runId == null) {
            return "You must run this script when hacking a site."
        }
        return null
    }

    fun checkInNode(hackerState: HackerState): String? {
        checkInRun(hackerState)?.let { return it }
        if (hackerState.activity == HackerActivity.OUTSIDE || hackerState.currentNodeId == null) {
            return "You can only run this script inside a node."
        }
        return null
    }

    fun runForIceLayer(
        layerType: LayerType, argumentTokens: List<String>, hackerState: HackerState, executionForIceLayer: (IceLayer) -> TerminalLockState
    ): ScriptExecution {
        checkInNode(hackerState)?.let { return ScriptExecution(it) }
        val layerNumber = argumentTokens.firstOrNull() ?: return ScriptExecution("Provide the [primary]layer[/] to use this script on.")
        val node = nodeEntityService.getById(hackerState.currentNodeId!!)
        val layer = node.layers.find { it.level == layerNumber.toInt() } ?: return ScriptExecution("Layer not found.")

        val klass = iceService.klassFor(layerType)
        if (layer !is IceLayer || !klass.isInstance(layer)) return ScriptExecution("This script can only be used on ${iceService.nameFor(layerType)}.")
        if (layer.hacked) return ScriptExecution("This ICE has already been hacked.")

        return ScriptExecution {
            iceService.findOrCreateIceForLayer(layer)
            executionForIceLayer(layer)
        }
    }
}
