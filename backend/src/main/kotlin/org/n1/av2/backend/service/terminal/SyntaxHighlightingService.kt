package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.model.Syntax
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

/**
 * See the javadoc in Syntax for more info on how the syntaxes work.
 */
@Service
class SyntaxHighlightingService(
    private val currentUser: CurrentUserService,
    private val stompService: StompService,

    ) {

    fun sendForScan() {
        val map = HashMap<String, Syntax>()

        map["help"] = Syntax("u", "error s")
        map["autoscan"] = Syntax("u", "error s")
        map["attack"] = Syntax("u", "error s")
        map["scan"] = Syntax("u", "ok", "error s")
        map["dc"] = Syntax("u", "error s")
        map["/share"] = Syntax("u warn", "info", "error s")

        map["move"] = Syntax("error s", "error s")
        map["view"] = Syntax("error s", "error s")
        map["hack"] = Syntax("error s", "error s")

        sendSyntaxHighlighting(map, currentUser.userId, stompService)
    }

    fun sendForAttack() {
        val map = HashMap<String, Syntax>()

        map["help"] = Syntax("u", "error s")
        map["move"] = Syntax("u", "ok", "error s")
        map["view"] = Syntax("u", "error s")
        map["hack"] = Syntax("u", "primary", "error s")
        map["dc"] = Syntax("u", "error s")
        map["/share"] = Syntax("u warn", "info", "error s")

        sendSyntaxHighlighting(map, currentUser.userId, stompService)
    }

    private class SyntaxHighlightingMessage(val terminalId: String, val highlighting: Map<String, Syntax>)

    fun sendSyntaxHighlighting(highlighting: Map<String, Syntax>, userId: String, stompService: StompService) {
        val message = SyntaxHighlightingMessage("main", highlighting)
        stompService.toUser(userId, ReduxActions.SERVER_TERMINAL_SYNTAX_HIGHLIGHTING, message)
    }

}

