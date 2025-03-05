package org.n1.av2.run.terminal

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class TerminalController(
    val userTaskRunner: UserTaskRunner,
    val terminalService: TerminalService
) {


    data class TerminalCommand(val runId: String, val command: String)

    @MessageMapping("/terminal/main")
    fun terminalMain(terminalCommand: TerminalCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/terminal/main", userPrincipal) { terminalService.processCommand(terminalCommand.runId, terminalCommand.command) }
    }

}
