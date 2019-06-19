package org.n1.av2.backend.web.ws

import org.n1.av2.backend.engine.SerializingExecutor
import org.n1.av2.backend.service.terminal.TerminalService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class TerminalController(
        val executor: SerializingExecutor,
        val terminalService: TerminalService
        ) {


    data class TerminalCommand(val runId: String, val command: String)
    @MessageMapping("/terminal/main")
    fun terminalMain(terminalCommand: TerminalCommand, principal: Principal) {
        executor.run(principal) { terminalService.processCommand(terminalCommand.runId, terminalCommand.command) }
    }

}