package org.n1.av2.script.effect.helper

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.Layer
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service

@Service
class ScriptEffectHelper(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
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

    class RunOnLayerResult(val layer: Layer?, val node: Node?, val execution: ScriptExecution?) {
        constructor(errorMessage: String) : this(null, null, ScriptExecution(errorMessage))
    }

    fun runOnLayer(argumentTokens: List<String>, hackerState: HackerState): RunOnLayerResult {
        checkInNode(hackerState)?.let { return RunOnLayerResult(it) }
        val layerInput = argumentTokens.firstOrNull() ?: return RunOnLayerResult("Provide the [primary]layer[/] to use this script on.")
        val layerNumber = layerInput.toIntOrNull() ?: return RunOnLayerResult("Provide the [primary]layer[/] to use this script on, this must be a number.")
        val node = nodeEntityService.getById(hackerState.currentNodeId!!)
        val layer = node.layers.find { it.level == layerNumber.toInt() } ?: return RunOnLayerResult("Layer not found.")
        return RunOnLayerResult(layer, node, null)
    }

}
