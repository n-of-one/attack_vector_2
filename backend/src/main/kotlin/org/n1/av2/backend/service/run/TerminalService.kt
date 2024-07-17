package org.n1.av2.backend.service.run

import org.n1.av2.backend.entity.run.HackerActivity
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.run.terminal.inside.InsideTerminalService
import org.n1.av2.backend.service.run.terminal.outside.OutsideTerminalService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service
import java.util.*

const val TERMINAL_MAIN = "main"


@Service
class TerminalService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val outsideTerminalService: OutsideTerminalService,
    private val insideTerminalService: InsideTerminalService,
    private val stompService: StompService,
) {

    private val logger = mu.KotlinLogging.logger {}

    fun processCommand(runId: String, command: String) {
        if (command.trim().isBlank()) {
            stompService.reply(ServerActions.SERVER_TERMINAL_RECEIVE,
                StompService.TerminalReceive(TERMINAL_MAIN, emptyArray(), false)
            )
            return
        }
        val type = hackerStateEntityService.retrieveForCurrentUser().activity
        when (type) {
            HackerActivity.OUTSIDE -> outsideTerminalService.processCommand(runId, command)
            HackerActivity.INSIDE -> insideTerminalService.processCommand(runId, command)

            else -> {
                logger.error("Received terminal command for user that is doing: ${type}")
            }
        }
    }
}
