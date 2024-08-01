package org.n1.av2.run.terminal

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.scanning.InitiateScanService
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service

@Service
class CommandScanService(
    private val connectionService: ConnectionService,
    private val runEntityService: RunEntityService,
    private val nodeEntityService: NodeEntityService,
    private val initiateScanService: InitiateScanService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
) {


    fun processScanFromOutside(run: Run) {
        val networkId = sitePropertiesEntityService.getBySiteId(run.siteId).startNodeNetworkId

        val node = nodeEntityService.findByNetworkId(run.siteId, networkId)
            ?: return connectionService.replyTerminalReceive("[error]Network error[/] no route to this site.")

        connectionService.replyTerminalSetLocked(true)
        initiateScanService.scanFromOutside(run, node)
    }


    fun processScanFromInside(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (tokens.size > 1) {
            connectionService.replyTerminalReceive("ignoring arguments, scanning current node")
        }
        connectionService.replyTerminalSetLocked(true)
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
        connectionService.replyTerminalReceive("Quickscanned.")
    }
}
