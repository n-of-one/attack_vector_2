package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.run.StartAttackService
import org.n1.av2.backend.service.scan.ScanningService
import org.springframework.stereotype.Service

@Service
class ScanTerminalService(
    private val scanningService: ScanningService,
    private val runEntityService: RunEntityService,
    private val nodeEntityService: NodeEntityService,
    private val socialTerminalService: SocialTerminalService,
    private val startAttackService: StartAttackService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val runService: RunService,
    private val stompService: StompService,
    private val config: ServerConfig,
    ) {


    fun processCommand(runId: String, command: String) {
        val tokens = command.split(" ")
        when (tokens[0]) {
            "help" -> processHelp()
            "autoscan" -> processAutoScan(runId)
            "scan" -> processScan(runId, tokens)
            "/share" -> socialTerminalService.processShare(runId, tokens)
            "servererror" -> error("gah")
            "quickscan", "qs" -> processQuickscan(runId)
            "attack" -> processAttack(runId, false)
            "quickattack", "qa" -> processAttack(runId, true)
            "move", "view", "hack" -> reportHackCommand()
            else -> stompService.replyTerminalReceive("Unknown command, try [u]help[/].")
        }
    }

    private fun reportHackCommand() {
        stompService.replyTerminalReceive("[warn]still scanning[/] - First initiate the attack with: [u]attack[/]")
    }

    private fun processAutoScan(runId: String) {
        stompService.replyTerminalReceiveAndLocked(true, "Autoscan started. [i]Click on nodes for information retreived by scan.[/]")
        scanningService.performAutoScan(runId)
    }

    private fun processHelp() {
        stompService.replyTerminalReceive(
                "Command options:",
                " [u]autoscan",
                " [u]attack",
                " [u]scan[/] [ok]<network id>[/]   -- for example: [u]scan[/] [ok]00",
                " [u]/share[/u] [info]<user name>")
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

        val status: NodeScanStatus = run.nodeScanById[node.id]!!.status
        if (status == NodeScanStatus.UNDISCOVERED_0) {
            return reportNodeNotFound(networkId)
        }

        stompService.replyTerminalSetLocked(true)
        scanningService.performManualScan(run, node, status)
    }

    private fun reportNodeNotFound(networkId: String) {
        stompService.replyTerminalReceive("Node [ok]${networkId}[/] not found.")
    }

    fun processQuickscan(runId: String) {
        scanningService.quickScan(runId)
    }

    private fun processAttack(runId: String, quick: Boolean) {
        stompService.replyTerminalSetLocked(true)
        startAttackService.startAttack(runId, quick)
    }





}