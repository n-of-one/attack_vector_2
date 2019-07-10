package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.model.Syntax
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService


private class SyntaxHighlightingMessage(val terminalId: String, val highlighting: Map<String, Syntax>)

fun sendSyntaxHighlighting(highlighting: Map<String, Syntax>, stompService: StompService) {
    val message = SyntaxHighlightingMessage("main", highlighting)
    stompService.toUser(ReduxActions.SERVER_TERMINAL_SYNTAX_HIGHLIGHTING, message)
}
