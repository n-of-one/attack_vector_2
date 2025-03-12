package org.n1.av2.layer.other.script

import org.n1.av2.platform.connection.ConnectionService
import org.springframework.stereotype.Service

@Service
class ScriptInteractionLayerService(
    private val connectionService: ConnectionService,
) {

    fun hack() {
        connectionService.replyTerminalReceive("[warn]Unsupported protocols[/a]. This layer uses non-default protocols.", "")
    }
}
