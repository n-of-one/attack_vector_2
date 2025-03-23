package org.n1.av2.script.effect.positive

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.scanning.InitiateScanService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.NodeAccessHelper
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.SCAN_ICE_NODE
 */
@Service
class ScanBeyondIceNodeEffectService(
    private val runEntityService: RunEntityService,
    private val initiateScanService: InitiateScanService,
    private val nodeEntityService: NodeEntityService,
    private val nodeAccessHelper: NodeAccessHelper,
    private val scriptEffectHelper: ScriptEffectHelper,
) : ScriptEffectInterface {

    override val name = "Scan beyond ICE node"
    override val defaultValue = "00:01:00"
    override val gmDescription = "When running this script on a node with ICE, it will scan beyond that node.."

    override fun playerDescription(effect: ScriptEffect) = "Scan beyond a node with ICE."

    override fun validate(effect: ScriptEffect) = null

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        scriptEffectHelper.checkInRun(hackerState)?.let { return ScriptExecution(it) }
        val networkId = argumentTokens.firstOrNull() ?: return ScriptExecution("Provide the [ok]network id[/] of the node to scan.")

        val targetNode = nodeEntityService.findByNetworkId(hackerState.siteId!!, networkId)
        nodeAccessHelper.checkNodeRevealed(targetNode, networkId, hackerState.runId!!)?.let { return ScriptExecution(it) }

        return ScriptExecution {
            val run = runEntityService.getByRunId(hackerState.runId)
            initiateScanService.scanIgnoringIceAtTargetNode(run, null, targetNode!!)
        }
    }

}
