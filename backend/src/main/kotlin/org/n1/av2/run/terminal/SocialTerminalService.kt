package org.n1.av2.run.terminal

import org.n1.av2.larp.LarpService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.run.runlink.RunLinkService
import org.springframework.stereotype.Service

@Service
class SocialTerminalService(
    private val connectionService: ConnectionService,
    private val userEntityService: UserEntityService,
    private val runLinkService: RunLinkService,
    private val larpService: LarpService,
        ) {

    fun processShare(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            connectionService.replyTerminalReceive("[warn]incomplete[/] - share this run with whom?  -- try [u warn]/share [info]<username>[/] or [u warn]/share [info]<username1> <username2>[/].")
            return
        }

        val userNames = tokens.drop(1).map { it.replace(",", "") }

        userNames.forEach {userName ->
            shareWithUser(userName, runId)
        }

    }

    private fun shareWithUser(userName: String, runId: String) {
        val user = userEntityService.findByNameIgnoreCase(userName)
        if (user == null) {
            connectionService.replyTerminalReceive("[warn]not found[/] - user [info]${userName}[/] not found.")
        } else {
            if (larpService.processShare(runId, user)) {
                return
            }
            runLinkService.shareRun(runId, user, true)
        }
    }

}
