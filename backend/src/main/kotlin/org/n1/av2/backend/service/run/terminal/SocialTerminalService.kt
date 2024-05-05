package org.n1.av2.backend.service.run.terminal

import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.service.larp.LarpService
import org.n1.av2.backend.service.larp.frontier.LolaService
import org.n1.av2.backend.service.site.RunLinkService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class SocialTerminalService(
    private val stompService: StompService,
    private val userEntityService: UserEntityService,
    private val runLinkService: RunLinkService,
    private val larpService: LarpService,
        ) {

    fun processShare(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            stompService.replyTerminalReceive("[warn]incomplete[/] - share this run with whom?  -- try [u warn]/share [info]<username>[/] or [u warn]/share [info]<username1> <username2>[/].")
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
            stompService.replyTerminalReceive("[warn]not found[/] - user [info]${userName}[/] not found.")
        } else {
            if (larpService.processShare(runId, user)) {
                return
            }
            runLinkService.shareRun(runId, user, true)
        }
    }

}
