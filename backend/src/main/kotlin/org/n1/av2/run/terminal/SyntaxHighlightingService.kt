package org.n1.av2.run.terminal

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
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

    constructor(first: String): this(listOf<String>(first), "error s")
    constructor(first: String, second: String ): this(listOf<String>(first, second), "error s")

}

@Service
class SyntaxHighlightingService(
    private val connectionService: ConnectionService,
    ) {

    private val syntaxOutside = HashMap<String, Syntax>()
    private val syntaxInside = HashMap<String, Syntax>()

    init {
        syntaxOutside["help"] = Syntax("u", "u")
        syntaxOutside["scan"] = Syntax("u")
        syntaxOutside["dc"] = Syntax("u", "error s")
        syntaxOutside["/share"] = Syntax(listOf("u warn"), "info")

        syntaxOutside["attack"] = Syntax("u")

        syntaxOutside["move"] = Syntax("error s")
        syntaxOutside["view"] = Syntax("error s")
        syntaxOutside["hack"] = Syntax("error s")
        syntaxOutside["password"] = Syntax("error s")



        syntaxInside["help"] = Syntax("u", "u")
        syntaxInside["scan"] = Syntax("u")
        syntaxInside["dc"] = Syntax("u")
        syntaxInside["/share"] = Syntax(listOf("u warn"), "info")

        syntaxInside["move"] = Syntax("u", "ok")
        syntaxInside["view"] = Syntax("u")
        syntaxInside["hack"] = Syntax("u", "primary")
        syntaxInside["password"] = Syntax("u", "primary")
    }


    fun sendForOutside() {
        connectionService.reply(ServerActions.SERVER_TERMINAL_SYNTAX_HIGHLIGHTING, "highlighting" to syntaxOutside, "terminalId" to "main")
    }

    fun sendForInside() {
        connectionService.reply(ServerActions.SERVER_TERMINAL_SYNTAX_HIGHLIGHTING, "highlighting" to syntaxInside, "terminalId" to "main")
    }

}


