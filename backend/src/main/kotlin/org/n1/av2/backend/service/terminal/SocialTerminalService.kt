package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.scan.ScanInfoService
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class SocialTerminalService(
    val stompService: StompService,
    val currentUserService: CurrentUserService,
    val userEntityService: UserEntityService,
    val scanInfoService: ScanInfoService
        ) {

    fun processShare(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            stompService.replyTerminalReceive("[warn]incomplete[/] - share this scan with who?  -- try [u warn]/share [info]<username>[/].")
            return
        }
        val userName = tokens.stream().skip(1).collect(Collectors.joining(" "))
        val user = userEntityService.findByName(userName)
        if (user == null) {
            stompService.replyTerminalReceive("[warn]not found[/] - user [info]${userName}[/] not found.")
            return
        }
        scanInfoService.shareScan(runId, user)
    }

}