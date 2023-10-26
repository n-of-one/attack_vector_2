package org.n1.av2.backend.service.run.terminal

import org.n1.av2.backend.entity.run.HackerStateRunning
import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.service.run.terminal.scanning.ScanningService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class CommandScanService(
    private val stompService: StompService,
    private val runEntityService: RunEntityService,
    private val nodeEntityService: NodeEntityService,
    private val scanningService: ScanningService,
) {


    fun processScanFromOutside(run: Run, tokens: List<String>) {
        if (tokens.size == 1) {
            stompService.replyTerminalReceive("[warn]error[/] - Missing [ok]<network id>[/], for example: [u]scan[/] [ok]00[/] . Or did you mean [u]autoscan[/]?")
            return
        }
        val networkId = tokens[1]

        val node = nodeEntityService.findByNetworkId(run.siteId, networkId)
            ?: if (networkId.length == 1) {
                return stompService.replyTerminalReceive("Node [ok]${networkId}[/] not found. Did you mean: [u]scan [ok]0${networkId}[/] ?")
            } else {
                return reportNodeNotFound(networkId)
            }

        if (!canScanNode(run, node)) {
            return reportNodeNotFound(networkId)
        }

        stompService.replyTerminalSetLocked(true)
        scanningService.scanFromOutside(run, node)
    }


    fun processScanFromInside(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (tokens.size > 1) {
            stompService.replyTerminalReceive("[warn]error[/] too many arguments. Can only scan current node. Try: [u]scan")
            return
        }
        stompService.replyTerminalSetLocked(true)
        val run = runEntityService.getByRunId(runId)
        val node = nodeEntityService.findById(state.currentNodeId)
        scanningService.scanFromInside(run, node)
    }


    private fun canScanNode(run: Run, node: Node): Boolean {
        val status: NodeScanStatus = run.nodeScanById[node.id]!!.status

        return status != NodeScanStatus.UNDISCOVERED_0 && status != NodeScanStatus.UNCONNECTABLE_1
    }

    private fun reportNodeNotFound(networkId: String) {
        stompService.replyTerminalReceive("Node [ok]${networkId}[/] not found.")
    }

    fun processQuickScan(run: Run) {
        scanningService.quickScan(run)
    }
}