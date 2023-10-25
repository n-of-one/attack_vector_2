package org.n1.av2.backend.service.run.outside

import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.service.run.outside.scanning.ScanningService
import org.n1.av2.backend.service.run.terminal.SocialTerminalService
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.service.util.TimeService
import org.springframework.stereotype.Service

@Service
class OutsideTerminalService(
    private val scanningService: ScanningService,
    private val nodeEntityService: NodeEntityService,
    private val socialTerminalService: SocialTerminalService,
    private val startAttackService: StartAttackService,
    private val stompService: StompService,
    private val config: ServerConfig,
    private val runEntityService: RunEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val timeService: TimeService,

    ) {


    fun processCommand(runId: String, command: String) {
        val tokens = command.split(" ")
        when (tokens[0]) {
            "help" -> processHelp()
            "scan" -> doIfSiteNotShutdown(runId) {processScan(runId, tokens) }
            "/share" -> socialTerminalService.processShare(runId, tokens)
            "quickscan", "qs" -> doIfSiteNotShutdown(runId) { processQuickscan(runId) }
            "attack" -> doIfSiteNotShutdown(runId) { processAttack(runId, false) }
            "quickattack", "qa" -> doIfSiteNotShutdown(runId) { processAttack(runId, true) }
            "move", "view", "hack", "connect" -> reportHackCommand()
            "servererror" -> error("gah")
            else -> stompService.replyTerminalReceive("Unknown command, try [u]help[/].")
        }
    }

    private fun reportHackCommand() {
        stompService.replyTerminalReceive("[warn]still scanning[/] - First initiate the attack with: [u]attack[/]")
    }


    private fun processHelp() {
        stompService.replyTerminalReceive(
            "Command options:",
            " [u]attack",
            " [u]scan[/] [ok]<network id>[/]   -- for example: [u]scan[/] [ok]00",
            " [u]/share[/u] [info]<user name>"
        )
        if (config.dev) {
            stompService.replyTerminalReceive(
                "",
                "[i]Available only during development and testing:[/]",
                " [u]quickscan[/] or [u]qs",
                " [u]quickattack[/] or [u]qa"
            )
        }
    }

    fun processScan(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            stompService.replyTerminalReceive("[warn]error[/] - Missing [ok]<network id>[/], for example: [u]scan[/] [ok]00[/] . Or did you mean [u]autoscan[/]?")
            return
        }
        val networkId = tokens[1]

        val run = runEntityService.getByRunId(runId)

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
        scanningService.performManualScan(run, node)
    }

    private fun canScanNode(run: Run, node: Node): Boolean {
        val status: NodeScanStatus = run.nodeScanById[node.id]!!.status

        return status != NodeScanStatus.UNDISCOVERED_0 && status != NodeScanStatus.UNCONNECTABLE_1

    }

    private fun reportNodeNotFound(networkId: String) {
        stompService.replyTerminalReceive("Node [ok]${networkId}[/] not found.")
    }

    fun processQuickscan(runId: String) {
        scanningService.quickScan(runId)
    }

    private fun processAttack(runId: String, quick: Boolean) {
        startAttackService.startAttack(runId, quick)
    }


    fun doIfSiteNotShutdown(runId: String, action: () -> Unit) {
        val run = runEntityService.getByRunId(runId)
        val siteProperties = sitePropertiesEntityService.getBySiteId(run.siteId)
        val now = timeService.now()
        if (siteProperties.shutdownEnd != null && now < siteProperties.shutdownEnd) {
            stompService.replyTerminalReceive("Connection refused. (site is in shutdown mode)")
            return
        }
        action.invoke()
    }

}