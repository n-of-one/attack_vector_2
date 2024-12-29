package org.n1.av2.run.terminal

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.run.terminal.inside.InsideTerminalService
import org.n1.av2.run.terminal.outside.OutsideTerminalService
import org.springframework.stereotype.Service

const val TERMINAL_MAIN = "main"


@Service
class TerminalService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val outsideTerminalService: OutsideTerminalService,
    private val insideTerminalService: InsideTerminalService,
    private val connectionService: ConnectionService,
    private val commandRunScriptService: CommandRunScriptService,
) {

    private val logger = mu.KotlinLogging.logger {}

    fun processCommand(runId: String, command: String) {
        if (command.trim().isBlank()) {
            connectionService.reply(
                ServerActions.SERVER_TERMINAL_RECEIVE,
                ConnectionService.TerminalReceive(TERMINAL_MAIN, emptyArray(), false)
            )
            return
        }

        val tokens = command.trim().split(" ")
        val commandAction = tokens[0].lowercase()

        val hackerSate = hackerStateEntityService.retrieveForCurrentUser()

        if (commandAction == "run") {
            commandRunScriptService.processRunScript(tokens, hackerSate)
            return
        }

        when (hackerSate.activity) {
            HackerActivity.OUTSIDE -> outsideTerminalService.processCommand(runId, commandAction, tokens)
            HackerActivity.INSIDE -> insideTerminalService.processCommand(runId, commandAction, tokens)

            else -> {
                logger.error("Received terminal command for user that is doing: ${hackerSate.activity}")
            }
        }
    }


}
