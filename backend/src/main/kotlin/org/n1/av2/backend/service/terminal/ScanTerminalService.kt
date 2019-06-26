package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.config.MyEnvironment
import org.n1.av2.backend.model.Syntax
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.scan.ScanningService
import org.n1.av2.backend.service.user.HackerActivityService
import org.springframework.stereotype.Service

@Service
class ScanTerminalService(val scanningService: ScanningService,
                          val stompService: StompService,
                          val currentUserService: CurrentUserService,
                          val userActivityService: HackerActivityService,
                          val socialTerminalService: SocialTerminalService,
                          val hackTerminalService: HackTerminalService,
                          val environment: MyEnvironment) {

    init {
        scanningService.scanTerminalService = this
    }

    fun processCommand(runId: String, command: String) {
        val tokens = command.split(" ")
        when (tokens[0]) {
            "help" -> processHelp()
            "dc" -> processDisconnect()
            "autoscan" -> processAutoScan(runId)
            "scan" -> processScan(runId, tokens)
            "/share" -> socialTerminalService.processShare(runId, tokens)
            "servererror" -> error("gah")
            "quickscan" -> processQuickscan(runId)
            "attack" -> processAttack(runId)
            else -> stompService.terminalReceive("Unknown command, try [u]help[/].")
        }
    }

    private fun processAutoScan(runId: String) {
        stompService.terminalReceive("Autoscan started. [i]Click on nodes for information retreived by scan.[/]")
        scanningService.launchProbe(runId, true)
    }

    private fun processDisconnect() {
        stompService.toUser(ReduxActions.SERVER_USER_DC, "-")
    }

    private fun processHelp() {
        stompService.terminalReceive(
                "Command options:",
                " [u]autoscan",
                " [u]attack",
                " [u]scan [ok]<network id>[/]   -- for example: [u]scan [ok]00",
                " [u]dc",
                " [u]/share [info]<user name>")
        if (environment.dev) {
            stompService.terminalReceive(
                    "",
                    "[i]Available only during development and testing:[/]",
                    " [u]quickscan"
            )
        }
    }

    fun processScan(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            stompService.terminalReceive("Missing [ok]<network id>[/], for example: [u]scan[/] [ok]00[/] . Or did you mean [u]autoscan[/]?")
            return
        }
        val networkId = tokens[1]
        scanningService.launchProbeAtNode(runId, networkId)
    }

    fun processQuickscan(runId: String) {
        scanningService.quickScan(runId)
    }

    data class StartRun(val userId: String)
    private fun processAttack(runId: String) {
        val data = StartRun(currentUserService.userId)
        userActivityService.startActivityHacking(runId)
        hackTerminalService.sendSyntaxHighlighting()
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_ENTER_RUN, data)
    }

    fun sendSyntaxHighlighting() {
        val map = HashMap<String, Syntax>()

        map["help"] = Syntax("u", "error s")
        map["autoscan"] = Syntax("u", "error s")
        map["attack"] = Syntax("u", "error s")
        map["scan"] = Syntax("u", "ok", "error s")
        map["dc"] = Syntax("u", "error s")
        map["/share"] = Syntax("u warn", "info", "error s")

        stompService.toUser(ReduxActions.SERVER_TERMINAL_SYNTAX_HIGHLIGHTING, map)
    }


}