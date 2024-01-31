package org.n1.av2.backend.service.run.terminal

import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class CommandHelpService(
    private val stompService: StompService,
    private val config: ServerConfig,
    ) {

    fun processHelp(inside: Boolean, tokens: List<String>) {
        if (tokens.size == 1 || tokens[1] != "shortcuts") {
            if (inside) {
                processHelpInside()
            } else {
                processHelpOutside()
            }
        } else {
            processHelpKeys()
        }
    }

    private fun processHelpOutside() {
        stompService.replyTerminalReceive(
            "You are outside of the site.",
            "Here you can examine the site before entering to plan your attack. Click on a node in the map to see what layers it contains.",
            "",
            "There are only a few commands you can use when outside:",
            "",
            "[u]scan[/]",
            "Scan the site to reveal nodes. Does not scan beyond ICE nodes.",
            "This is usually the first thing you do when you arrive at a new site, to gain information about it.",
            "",
            "[u]attack",
            "Enter the site and start the attack..",
            "",
            "[u]/share[/] [info]<user name>[/] (optionally more usernames)",
            "Share your run with one or more hackers so they can join you.",
            "",
        )
        if (config.dev) {
            stompService.replyTerminalReceive(
                "",
                "[i]Available only during development and testing:[/]",
                " [u]quickscan[/] or [u]qs",
                " [u]quickattack[/] or [u]qa",
                "",
            )
        }
    }


    private fun processHelpInside() {
        stompService.replyTerminalReceive(
            "You are inside the site.",
            "",
            "These are the most common commands you will use:",
            "",
            "[u]view",
            "Shows the current layers in the node you are in. You can also click on a node on the map.",
            "",
            "[u]move[/] [ok]<network id>[/]     -- for example: [u]mv[ok] 01",
            "Move to an adjacent node.",
            "",
            "[u]hack[/] [primary]<layer>[/]          -- for example: [u]hack[primary] 1",
            "Use this to hack a layer, for example an ICE layer or a database.",
            "This is the primary way to interact with layers, works on almost all layers.",
            "",
            "",
            "These commands are only used occasionally:",
            "",
            "[u]scan[/]",
            "Scan to reveal new nodes once ICE has been hacked.",
            "",
            "[u]password[/] [primary]<layer>[/]      -- for example: [u]password[primary] 1",
            "Opens the password interface for ICE, to provide a password.",
            "",
            "[u]dc",
            "Disconnect from the site, stop your attack. You can also click on 'Home' in the menu at the bottom.",
            "",
            "[u]/share[/] [info]<user name>[/] (optionally more usernames)",
            "Share your run with one or more hackers so they can join you.",
            "",
            "For information on shortcuts, type: [u]help[/] [u]shortcuts[/]",
            "",
        )
    }

    private fun processHelpKeys() {
        stompService.replyTerminalReceive(
            "Terminal shortcuts:",
            "",
            "Arrow up and down: scroll through previous commands.",
            "Ctrl-L: clear the terminal.",
            "",
        )
    }
}