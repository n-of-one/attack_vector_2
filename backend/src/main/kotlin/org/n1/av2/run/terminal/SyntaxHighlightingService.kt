package org.n1.av2.run.terminal

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.springframework.stereotype.Service

/**
 * A Syntax has this form:
 *
 * "hack" -> Syntax("t_b", "t_primary", "t_error t_s")
 *
 * This translates to
 *
 * Syntax {
 *   main: ["t_b", "t_primary"],
 *   rest: "t_error t_s"
 * }
 *
 * This means:
 * - the first word will be style t_b=bold (the command "hack")
 * - the second word will be marked with style t_primary=primary (the name of the site to hack)
 * - any additional parameters will be marked with styles ts_error=red and t_s=strikethrough to mark them as not useful
 *
 * The css for this is located in terminal.css
 */

data class Syntax(val main: List<String>, val rest: String) {

    constructor(first: String): this(listOf<String>(first), "t_error t_s")
    constructor(first: String, second: String ): this(listOf<String>(first, second), "t_error t_s")

}

@Service
class SyntaxHighlightingService(
    private val connectionService: ConnectionService,
    ) {

    private val syntaxOutside = HashMap<String, Syntax>()
    private val syntaxInside = HashMap<String, Syntax>()

    init {
        syntaxOutside["help"] = Syntax("t_b", "t_b")
        syntaxOutside["scan"] = Syntax("t_b")
        syntaxOutside["dc"] = Syntax("t_b", "t_error t_s")
        syntaxOutside["run"] = Syntax(listOf("t_b", "t_primary"), "t_b")
        syntaxOutside["/share"] = Syntax(listOf("t_b t_warn"), "t_info")

        syntaxOutside["attack"] = Syntax("t_b")

        syntaxOutside["move"] = Syntax("t_error t_s")
        syntaxOutside["view"] = Syntax("t_error t_s")
        syntaxOutside["hack"] = Syntax("t_error t_s")
        syntaxOutside["password"] = Syntax("t_error t_s")




        syntaxInside["help"] = Syntax("t_b", "t_b")
        syntaxInside["scan"] = Syntax("t_b")
        syntaxInside["dc"] = Syntax("t_b")
        syntaxInside["/share"] = Syntax(listOf("t_b t_warn"), "t_info")

        syntaxInside["move"] = Syntax("t_b", "t_ok")
        syntaxInside["view"] = Syntax("t_b")
        syntaxInside["hack"] = Syntax("t_b", "t_primary")
        syntaxInside["password"] = Syntax("t_b", "t_primary")
        syntaxInside["run"] = Syntax(listOf("t_b", "t_primary"), "t_b")

    }


    fun sendForOutside() {
        connectionService.reply(ServerActions.SERVER_TERMINAL_SYNTAX_HIGHLIGHTING, "highlighting" to syntaxOutside, "terminalId" to "main")
    }

    fun sendForInside() {
        connectionService.reply(ServerActions.SERVER_TERMINAL_SYNTAX_HIGHLIGHTING, "highlighting" to syntaxInside, "terminalId" to "main")
    }

}


