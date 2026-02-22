package org.n1.av2.run.terminal.inside

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.run.RunService
import org.n1.av2.run.terminal.SyntaxHighlightingService
import org.springframework.stereotype.Service

@Service
class CommandDisconnectService(
    private val runService: RunService,
    private val insideTerminalHelper: InsideTerminalHelper,
    private val syntaxHighlightingService: SyntaxHighlightingService,
) {

    fun disconnect(hackerState: HackerStateRunning) {
        if (!insideTerminalHelper.verifyInside(hackerState)) return

        runService.hackerDisconnect(hackerState.toState(), "Disconnected")
        syntaxHighlightingService.sendForOutside()
    }
}
