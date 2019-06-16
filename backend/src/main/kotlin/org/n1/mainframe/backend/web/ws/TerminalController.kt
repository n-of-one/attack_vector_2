package org.n1.mainframe.backend.web.ws

import org.n1.mainframe.backend.engine.SerializingExecutor
import org.n1.mainframe.backend.service.terminal.TerminalService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class TerminalController(
        val executor: SerializingExecutor,
        val terminalService: TerminalService
        ) {


    data class TerminalCommand(val scanId: String, val command: String)
    @MessageMapping("/terminal/main")
    fun terminalMain(terminalCommand: TerminalCommand, principal: Principal) {
        executor.run(principal) { terminalService.processCommand(terminalCommand.scanId, terminalCommand.command) }
    }

}