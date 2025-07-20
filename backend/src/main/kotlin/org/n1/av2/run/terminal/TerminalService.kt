package org.n1.av2.run.terminal

import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.run.terminal.generic.*
import org.n1.av2.run.terminal.inside.*
import org.n1.av2.run.terminal.inside.skillbased.CommandJumpToHackerService
import org.n1.av2.run.terminal.inside.skillbased.CommandUndoTripwireService
import org.n1.av2.run.terminal.inside.skillbased.CommandWeakenService
import org.n1.av2.run.terminal.outside.CommandStartAttackService
import org.springframework.stereotype.Service

const val TERMINAL_MAIN = "main"

const val UNKNOWN_COMMAND_RESPONSE = "Unknown command, try [b]help[/]."

const val MISSING_SKILL_RESPONSE = "Command not installed. [mute](Missing skill)"

@Service
class TerminalService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val connectionService: ConnectionService,

    private val commandHelpService: CommandHelpService,
    private val socialTerminalService: SocialTerminalService,
    private val commandScriptService: CommandScriptService,

    private val commandScanService: CommandScanService,
    private val commandStartAttackService: CommandStartAttackService,

    private val commandHackService: CommandHackService,
    private val commandMoveService: CommandMoveService,
    private val commandViewService: CommandViewService,
    private val commandDebugService: CommandDebugService,

    private val commandWeakenService: CommandWeakenService,
    private val commandUndoTripwireService: CommandUndoTripwireService,
    private val commandJumpToHackerService: CommandJumpToHackerService,

    private val commandDisconnectService: CommandDisconnectService,

    private val devCommandHelper: DevCommandHelper,
) {

    fun processCommand(command: String) {

        val tokens = command.split(" ").map { it.trim() }
        val commandAction = tokens.first().lowercase()

        if (commandAction.isBlank()) {
            connectionService.reply( ServerActions.SERVER_TERMINAL_RECEIVE, ConnectionService.TerminalReceive(TERMINAL_MAIN, emptyArray(), false) )
            return
        }

        val arguments = tokens.drop(1)
        val hackerState = hackerStateEntityService.retrieveForCurrentUser().toRunState()

        processCommand(commandAction, arguments, hackerState)
    }

    private fun processCommand(commandAction: String, arguments: List<String>, hackerState: HackerStateRunning) {
        when (commandAction) {

            "move" -> commandMoveService.processCommand(arguments, hackerState)
            "view" -> commandViewService.process(hackerState)
            "hack" -> commandHackService.processHackCommand(arguments, hackerState)

            "attack" -> commandStartAttackService.startAttack(hackerState)
            "run" -> commandScriptService.processRunScript(arguments, hackerState)
            "scan" -> commandScanService.processScan(arguments, hackerState)

            "dc" -> commandDisconnectService.disconnect(hackerState)
            "password" -> commandHackService.processPasswordCommand(arguments, hackerState)
            "/share" -> socialTerminalService.processShare(arguments, hackerState)
            "/download-script" -> commandScriptService.processDownloadScript(arguments)
            "help" -> commandHelpService.processHelp(arguments, hackerState)

            "weaken" -> commandWeakenService.processWeaken(arguments, hackerState)
            "rollback" -> commandUndoTripwireService.processCommand(hackerState)
            "jump" -> commandJumpToHackerService.processCommand(arguments, hackerState)


            "qs" -> commandScanService.processQuickScan(hackerState)
            "qa" ->commandStartAttackService.startQuickAttack(hackerState)
            "qmove" -> commandMoveService.processQuickMove(arguments, hackerState)
            "qhack" -> commandHackService.processQuickHack(arguments, hackerState)
            "sweeperunblock" -> commandDebugService.processSweepUnblock(arguments, hackerState)
            "quick"-> devCommandHelper.setQuickPlaying()

            else -> connectionService.replyTerminalReceive(UNKNOWN_COMMAND_RESPONSE)
        }
    }

}
