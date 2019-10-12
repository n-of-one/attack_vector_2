package org.n1.av2.backend.service.terminal

import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.scan.ScanningService
import org.n1.av2.backend.service.user.UserService
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class SocialTerminalService(
        val stompService: StompService,
        val currentUserService: CurrentUserService,
        val userService: UserService,
        val scanningService: ScanningService
        ) {

    fun processShare(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            stompService.terminalReceiveCurrentUser("[warn]incomplete[/] - share this scan with who?  -- try [u warn]/share [info]<username>[/].")
            return
        }
        val userName = tokens.stream().skip(1).collect(Collectors.joining(" "))
        val user = userService.findByName(userName)
        if (user == null) {
            stompService.terminalReceiveCurrentUser("[warn]not found[/] - user [info]${userName}[/] not found.")
            return
        }
        scanningService.shareScan(runId, user)
    }

}