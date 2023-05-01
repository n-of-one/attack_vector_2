package org.n1.av2.backend.web.ws

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.terminal.TerminalService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class TerminalController(
    val taskRunner: TaskRunner,
    val terminalService: TerminalService
) {


    data class TerminalCommand(val runId: String, val command: String)

    @MessageMapping("/terminal/main")
    fun terminalMain(terminalCommand: TerminalCommand, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { terminalService.processCommand(terminalCommand.runId, terminalCommand.command) }
    }

}