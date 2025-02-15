package org.n1.av2.script.effect.positive

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.run.entity.NodeScanStatus
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.scanning.InitiateScanService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class ScanIceNodeEffectService(
    private val runEntityService: RunEntityService,
    private val initiateScanService: InitiateScanService,
    private val nodeEntityService: NodeEntityService,
) : ScriptEffectInterface {

    override val name = "Scan beyond ICE node"
    override val defaultValue = "00:01:00"
    override val gmDescription = "When running this script on a node with ICE, it will scan beyond that node.."

    override fun playerDescription(effect: ScriptEffect) = "Scan beyond a node with ICE."

    override fun validate(effect: ScriptEffect): String? {
        return null
    }

    override fun checkCanExecute(effect: ScriptEffect, tokens: List<String>, hackerSate: HackerState): String? {
        if (tokens.size < 3) {
            return "Provide the [ok]network id[/] of the node to scan."
        }
        val networkId = tokens[2]

        val runId = hackerSate.runId ?: return "Error, not in a run."
        val run = runEntityService.getByRunId(runId)


        val targetNode = nodeEntityService.findByNetworkId(run.siteId, networkId)?: error("Node not found for network ID: $networkId")

        val targetNodeStatus = run.nodeScanById[targetNode.id]?.status ?: return "Node [ok]$networkId[/] not found."
        if (targetNodeStatus == NodeScanStatus.UNDISCOVERED_0 || targetNodeStatus == NodeScanStatus.UNCONNECTABLE_1) {
            return "Node [ok]$networkId[/] not found."
        }
        return null
    }

    override fun execute(effect: ScriptEffect, tokens: List<String>, hackerSate: HackerState): TerminalLockState {
        val networkId = tokens[2]
        val run = runEntityService.getByRunId(hackerSate.runId!!)
        val targetNode = nodeEntityService.findByNetworkId(run.siteId, networkId) ?: error("Node not found for network ID: $networkId")

        initiateScanService.scanWithScript(run, null,targetNode )

        return TerminalLockState.LOCK
    }
}
