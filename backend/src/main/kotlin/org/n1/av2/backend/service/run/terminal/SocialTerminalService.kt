package org.n1.av2.backend.service.run.terminal

import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.service.site.RunLinkService
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class SocialTerminalService(
    val stompService: StompService,
    val currentUserService: CurrentUserService,
    val userEntityService: UserEntityService,
    val runLinkService: RunLinkService
        ) {

    fun processShare(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            stompService.replyTerminalReceive("[warn]incomplete[/] - share this run with whom?  -- try [u warn]/share [info]<username>[/] or [u warn]/share [info]<username1> <username2>[/].")
            return
        }

        val userNames = tokens.drop(1).map { it.replace(",", "") }

        userNames.forEach {userName ->
            val user = userEntityService.findByNameIgnoreCase(userName)
            if (user == null) {
                stompService.replyTerminalReceive("[warn]not found[/] - user [info]${userName}[/] not found.")
            }
            else {
                runLinkService.shareRun(runId, user)
            }
        }

    }

}