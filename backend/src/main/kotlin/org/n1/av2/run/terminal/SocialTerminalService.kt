package org.n1.av2.run.terminal

import org.n1.av2.larp.frontier.LOLA_USER_NAME
import org.n1.av2.larp.frontier.LolaService
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.run.runlink.RunLinkService
import org.n1.av2.script.ScriptService
import org.springframework.stereotype.Service

@Service
class SocialTerminalService(
    private val connectionService: ConnectionService,
    private val userEntityService: UserEntityService,
    private val runLinkService: RunLinkService,
    private val configService: ConfigService,
    private val lolaService: LolaService,
    private val scriptService: ScriptService,
) {

    fun processShare(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            connectionService.replyTerminalReceive("Missing one more more user names. For example /share [info]<username1> <username2>[/].")
            return
        }

        val userNames = tokens.drop(1).map { it.replace(",", "") }

        userNames.forEach { userName ->
            shareWithUser(userName, runId)
        }

    }

    private fun shareWithUser(userName: String, runId: String) {
        val user = userEntityService.findByNameIgnoreCase(userName)
        if (user == null) {
            connectionService.replyTerminalReceive(hackerNotFound(userName))
            return
        }

        if (user.name.uppercase() == LOLA_USER_NAME && configService.getAsBoolean(ConfigItem.LARP_SPECIFIC_FRONTIER_LOLA_ENABLED)) {
            lolaService.share(user, runId)
        } else {
            runLinkService.shareRun(runId, user, true)
        }
    }

    fun downloadScript(tokens: List<String>) {
        if (!configService.getAsBoolean(ConfigItem.HACKER_SCRIPT_LOAD_DURING_RUN)) {
            connectionService.replyTerminalReceive(UNKNOWN_COMMAND_RESPONSE)
            return
        }

        if (tokens.size == 1) {
            connectionService.replyTerminalReceive("Missing [primary]<script code>[/] for example /download-script [primary]1234-abcd[/].")
            return
        }
        val scriptCode = tokens[1]
        scriptService.downloadScript(scriptCode, true)
    }

    companion object {
        fun hackerNotFound(userName: String) = "[warn]not found[/] - user [info]${userName}[/] not found."
    }

}
