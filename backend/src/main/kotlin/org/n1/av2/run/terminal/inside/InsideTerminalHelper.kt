package org.n1.av2.run.terminal.inside

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.platform.connection.ConnectionService
import org.springframework.stereotype.Service

@Service
class InsideTerminalHelper(
    private val connectionService: ConnectionService,
) {

    fun verifyInside(hackerState: HackerStateRunning): Boolean {
        if (hackerState.activity != HackerActivity.INSIDE) {
            connectionService.replyTerminalReceive("[warn]You are outside[/] - First, start the attack with: [b]attack[/]")
            return false
        }
        return true
    }

}
