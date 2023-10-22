package org.n1.av2.backend.service.run.terminal

import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

/**
 * A Syntax has this form:
 *
 * "hack" -> Syntax("u", "primary", "error s")
 *
 * This translates to
 *
 * Syntax {
 * main: ["u", "primary"],
 * rest: "error s"
 * }
 *
 * This means:
 * - the first word will be underlined (in this case the command "hack"
 * - the second word will be marked with style primary, the name of the site to hack
 * - any additional parameters will be marked with styles error (red), s (strikethrough) to mark them as not useful
 *
 * In the frontend this is implemented through css styles, and the syntax is prefixed with "terminal_style_", so
 * "u" becomes "terminal_style_u", which is defined in terminal.css .
 */

data class Syntax(val main: List<String>, val rest: String) {

    constructor(first: String, rest: String): this(listOf<String>(first), rest)
    constructor(first: String, second: String, rest: String): this(listOf<String>(first, second), rest)

}

@Service
class SyntaxHighlightingService(
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

        stompService.reply(ServerActions.SERVER_TERMINAL_SYNTAX_HIGHLIGHTING, "highlighting" to map, "terminalId" to "main")
    }

    fun sendForAttack() {
        val map = HashMap<String, Syntax>()

        map["help"] = Syntax("u", "error s")
        map["move"] = Syntax("u", "ok", "error s")
        map["view"] = Syntax("u", "error s")
        map["hack"] = Syntax("u", "primary", "error s")
        map["connect"] = Syntax("u", "primary", "error s")
        map["dc"] = Syntax("u", "error s")
        map["/share"] = Syntax("u warn", "info", "error s")

        stompService.reply(ServerActions.SERVER_TERMINAL_SYNTAX_HIGHLIGHTING, "highlighting" to map, "terminalId" to "main")
    }

}


