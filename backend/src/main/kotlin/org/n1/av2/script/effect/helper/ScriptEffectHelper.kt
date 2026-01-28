package org.n1.av2.script.effect.helper

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerStateRunning
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

    fun checkAtNonShutdownSite(hackerState: HackerStateRunning): String? {
        if (hackerState.activity == HackerActivity.OFFLINE ) {
            return "You must run this script when hacking a site."
        }

        val siteState = sitePropertiesEntityService.getBySiteId(hackerState.siteId)
        if (siteState.shutdownEnd != null) {
            return "Cannot run this script while the site is shut down."
        }
        return null
    }

    fun checkInNode(hackerState: HackerStateRunning): String? {
        if (hackerState.activity == HackerActivity.OUTSIDE) {
            return "You can only run this script inside a node."
        }
        return null
    }

    class RunOnLayerResult(val layer: Layer?, val node: Node?, val errorExecution: ScriptExecution?) {
        constructor(errorMessage: String) : this(null, null, ScriptExecution(errorMessage))
    }

    /** Check if the script can be run on the specified layer */
    fun runOnLayer(argumentTokens: List<String>, hackerState: HackerStateRunning): RunOnLayerResult {
        checkInNode(hackerState)?.let { return RunOnLayerResult(it) }
        requireNotNull(hackerState.currentNodeId)
        val layerInput = argumentTokens.firstOrNull() ?: return RunOnLayerResult("Provide the [primary]layer[/] to use this script on.")
        val layerNumber = layerInput.toIntOrNull() ?: return RunOnLayerResult("Provide the [primary]layer[/] to use this script on, this must be a number.")
        val node = nodeEntityService.getById(hackerState.currentNodeId)
        val layer = node.layers.find { it.level == layerNumber.toInt() } ?: return RunOnLayerResult("Layer not found.")
        return RunOnLayerResult(layer, node, null)
    }

}
