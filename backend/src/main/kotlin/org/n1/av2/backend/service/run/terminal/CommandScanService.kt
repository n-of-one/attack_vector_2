package org.n1.av2.backend.service.run.terminal

import org.n1.av2.backend.entity.run.HackerStateRunning
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.service.run.terminal.scanning.InitiateScanService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class CommandScanService(
    private val stompService: StompService,
    private val runEntityService: RunEntityService,
    private val nodeEntityService: NodeEntityService,
    private val initiateScanService: InitiateScanService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
) {


    fun processScanFromOutside(run: Run) {
        val networkId = sitePropertiesEntityService.getBySiteId(run.siteId).startNodeNetworkId

        val node = nodeEntityService.findByNetworkId(run.siteId, networkId)
            ?: return stompService.replyTerminalReceive("[error]Network error[/] no route to this site.")

        stompService.replyTerminalSetLocked(true)
        initiateScanService.scanFromOutside(run, node)
    }


    fun processScanFromInside(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (tokens.size > 1) {
            stompService.replyTerminalReceive("ignoring arguments, scanning current node")
        }
        stompService.replyTerminalSetLocked(true)
        val run = runEntityService.getByRunId(runId)
        val node = nodeEntityService.findById(state.currentNodeId)
        initiateScanService.scanFromInside(run, node)
    }

// TODO: use this code to implement architect scanning.

//    private fun canScanNode(run: Run, node: Node): Boolean {
//        val status: NodeScanStatus = run.nodeScanById[node.id]!!.status
//
//        return status != NodeScanStatus.UNDISCOVERED_0 && status != NodeScanStatus.UNCONNECTABLE_1
//    }
//
//    private fun reportNodeNotFound(networkId: String) {
//        stompService.replyTerminalReceive("Node [ok]${networkId}[/] not found.")
//    }

    fun processQuickScan(run: Run) {
        initiateScanService.quickScan(run)
    }
}