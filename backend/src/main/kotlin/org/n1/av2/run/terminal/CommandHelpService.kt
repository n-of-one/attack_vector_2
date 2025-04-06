package org.n1.av2.run.terminal

import org.n1.av2.hacker.hacker.Hacker
import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hacker.HackerSkillType
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.springframework.stereotype.Service

@Service
class CommandHelpService(
    private val connectionService: ConnectionService,
    private val configService: ConfigService,
    private val currentUser: CurrentUserService,
    private val hackerEntityService: HackerEntityService,
) {

    fun processHelp(inside: Boolean, tokens: List<String>) {
        val hacker = hackerEntityService.findForUser(currentUser.userEntity)
        if (tokens.size == 1 || tokens[1] != "shortcuts") {
            if (inside) {
                processHelpInside(hacker)
            } else {
                processHelpOutside(hacker)
            }
        } else {
            processHelpKeys()
        }
    }

    private fun processHelpOutside(hacker: Hacker) {
        connectionService.replyTerminalReceive(
            "You are outside of the site.",
            "Here you can examine the site before entering to plan your attack. Click on a node in the map to see what layers it contains.",
            "",
            "There are only a few commands you can use when outside:",
            ""
        )
        if (hacker.hasSkill(HackerSkillType.SCAN)) {
            connectionService.replyTerminalReceive(
                "[b]scan[/]",
                "Scan the site to reveal nodes. Does not scan beyond ICE nodes.",
                "This is usually the first thing you do when you arrive at a new site, to gain information about it.",
                ""
            )
        }

        connectionService.replyTerminalReceive(
            "[b]attack",
            "Enter the site and start the attack..",
            "",
        )
        showRunScriptHelp(hacker)
        showShareHelp()
        showDownloadScriptHelp(hacker)

        if (configService.getAsBoolean(ConfigItem.DEV_HACKER_USE_DEV_COMMANDS)) {
            connectionService.replyTerminalReceive(
                "",
                "[i]Available only during development and testing:[/]",
                " [b]quickscan[/] or [b]qs",
                " [b]quickattack[/] or [b]qa",
                "",
            )
        }
    }

    private fun processHelpInside(hacker: Hacker) {
        connectionService.replyTerminalReceive(
            "You are inside the site.",
            "",
            "These are the most common commands you will use:",
            "",
            "[b]view",
            "Shows the current layers in the node you are in. You can also click on a node on the map.",
            "",
            "[b]move[/] [ok]<network id>[/]     -- for example: [b]move[ok] 01",
            "Move to an adjacent node.",
            "",
            "[b]hack[/] [primary]<layer>[/]          -- for example: [b]hack[primary] 1",
            "Use this to hack a layer, for example an ICE layer or a database.",
            "This is the primary way to interact with layers, works on almost all layers.",
            "",
            "",
            "These commands are only used occasionally:",
            ""
        )

        if (hacker.hasSkill(HackerSkillType.SCAN)) {
            connectionService.replyTerminalReceive(
                "[b]scan[/]",
                "Scan to reveal new nodes once ICE has been hacked.",
                "",
            )
        }
        showRunScriptHelp(hacker)
        connectionService.replyTerminalReceive(
            "[b]password[/] [primary]<layer>[/]      -- for example: [b]password[primary] 1",
            "Opens the password interface for ICE, to provide a password.",
            "",
            "[b]dc",
            "Disconnect from the site, stop your attack. You can also click on 'Home' in the menu at the bottom.",
            "",
        )
        showShareHelp()
        showDownloadScriptHelp(hacker)

        connectionService.replyTerminalReceive(
            "For information on shortcuts, type: [b]help[/] [b]shortcuts[/]",
            ""
        )
    }

    private fun processHelpKeys() =
        connectionService.replyTerminalReceive(
            "Terminal shortcuts:",
            "",
            "Arrow up and down: scroll through previous commands.",
            "Ctrl-L: clear the terminal.",
            "",
        )

    private fun showRunScriptHelp(hacker: Hacker) {
        if (hacker.hasSkill(HackerSkillType.SCRIPT_RAM)) {
            connectionService.replyTerminalReceive(
                "[b]run[/] [primary]<script code>[/]      -- for example: [b]run[primary] 1234-abcd",
                "Run a script that you have loaded in memory.",
                "",
            )
        }
    }

    private fun showDownloadScriptHelp(hacker: Hacker) {
        if (configService.getAsBoolean(ConfigItem.HACKER_SCRIPT_LOAD_DURING_RUN) && hacker.hasSkill(HackerSkillType.SCRIPT_RAM)) {
            connectionService.replyTerminalReceive(
                "[b]/download-script[/] [primary]<script code>[/] - download a script.",
                "You can download a script from another hacker who has offered it, or you can buy it from someone.",
                ""
            )
        }
    }

    private fun showShareHelp() =
        connectionService.replyTerminalReceive(
            "[b]/share[/] [info]<user name>[/] (optionally more usernames)",
            "Share your run with one or more hackers so they can join you.",
            "",
        )
}
