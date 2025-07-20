package org.n1.av2.run.terminal.generic

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.hacker.skill.Skill
import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.hacker.skill.SkillType.*
import org.n1.av2.hacker.skill.containsType
import org.n1.av2.platform.config.ConfigItem.DEV_HACKER_USE_DEV_COMMANDS
import org.n1.av2.platform.config.ConfigItem.HACKER_SCRIPT_LOAD_DURING_RUN
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.springframework.stereotype.Service

@Service
class CommandHelpService(
    private val connectionService: ConnectionService,
    private val configService: ConfigService,
    private val skillService: SkillService,
    ) {

    fun processHelp(arguments: List<String>, hackerState: HackerStateRunning) {
        val skills = skillService.findSkillsForUser(hackerState.userId)

        if (arguments.isEmpty() || arguments[0] != "shortcuts") {
            if (hackerState.activity == HackerActivity.INSIDE) {
                processHelpInside(skills)
            } else {
                processHelpOutside(skills)
            }
        } else {
            processHelpKeys()
        }
    }

    private fun processHelpOutside(skills: List<Skill>) {
        connectionService.replyTerminalReceive(
            "You are outside of the site.",
            "Here you can examine the site before entering to plan your attack. Click on a node in the map to see what layers it contains.",
            "",
            "There are only a few commands you can use when outside:",
            ""
        )
        if (skills.containsType(SCAN)) {
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
        showRunScriptHelp(skills)
        showShareHelp()
        showDownloadScriptHelp(skills)

        if (configService.getAsBoolean(DEV_HACKER_USE_DEV_COMMANDS)) {
            connectionService.replyTerminalReceive(
                "",
                "[i]Available only during development and testing:[/]",
                " [b]quickscan[/] or [b]qs",
                " [b]quickattack[/] or [b]qa",
                "",
            )
        }
    }

    private fun processHelpInside(skills: List<Skill>) {
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

        if (skills.containsType(SCAN)) {
            connectionService.replyTerminalReceive(
                "[b]scan[/]",
                "Scan to reveal new nodes once ICE has been hacked.",
                "",
            )
        }

        showRunScriptHelp(skills)
        showWeakenHelp(skills)
        showUndoTripwireHelp(skills)
        showJumpToHackerHelp(skills)

        connectionService.replyTerminalReceive(
            "[b]password[/] [primary]<layer>[/]      -- for example: [b]password[primary] 1",
            "Opens the password interface for ICE, to provide a password.",
            "",
            "[b]dc",
            "Disconnect from the site, stop your attack. You can also click on 'Home' in the menu at the bottom.",
            "",
        )
        showShareHelp()
        showDownloadScriptHelp(skills)

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

    private fun showRunScriptHelp(skills: List<Skill>) {
        if (skills.containsType(SCRIPT_RAM)) {
            connectionService.replyTerminalReceive(
                "[b]run[/] [primary]<script code>[/]      -- for example: [b]run [primary]1234-abcd",
                "Run a script that you have loaded in memory.",
                "",
            )
        }
    }

    private fun showWeakenHelp(skills: List<Skill>) {
        if (skills.containsType(WEAKEN)) {
            connectionService.replyTerminalReceive(
                "[b]weaken[/] [primary]<layer>[/]      -- for example: [b]weaken [primary]1",
                "Reduce the strength of the ICE layer.",
                "",
                "[b]weaken",
                "Show how many uses of weaken you have left.",
                ""
            )
        }
    }

    private fun showUndoTripwireHelp(skills: List<Skill>) {
        if (skills.containsType(UNDO_TRIPWIRE)) {
            connectionService.replyTerminalReceive(
                "[b]glitch[/]",
                "Move back to the previous node and cancel all timers you started by entering the current node. " +
                "Does not work if you did not start any timers in the current node.",
                "",
            )
        }
    }

    private fun showJumpToHackerHelp(skills: List<Skill>) {
        if (skills.containsType(JUMP_TO_HACKER)) {
            connectionService.replyTerminalReceive(
                "[b]jump[/] [info]<user name> -- for example: [b]jump [info]angler",
                "Jump to the node of another hacker. Ice does not block this movement.",
                "",
            )
        }
    }

    private fun showDownloadScriptHelp(skills: List<Skill>) {
        if (configService.getAsBoolean(HACKER_SCRIPT_LOAD_DURING_RUN) && skills.containsType(SCRIPT_RAM)) {
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
