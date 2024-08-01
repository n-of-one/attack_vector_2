package org.n1.av2.run.terminal.inside

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.layer.ice.sweeper.SweeperIceLayer
import org.n1.av2.layer.ice.sweeper.SweeperService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class CommandDebugService(
    private val connectionService: ConnectionService,
    private val sweeperService: SweeperService,
    private val nodeEntityService: NodeEntityService,

    ) {

    fun processSweepUnblock(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (tokens.size != 2 ) {
            return connectionService.replyTerminalReceive("Missing layer level.")
        }
        val level = tokens[1].toIntOrNull() ?: return connectionService.replyTerminalReceive("Layer ${tokens[1]} is not a valid layer.")
        val node = nodeEntityService.getById(state.currentNodeId)
        val layer = node.layers.find { it.level == level }!!

        if (layer !is SweeperIceLayer) return connectionService.replyTerminalReceive("Layer is not sweeper ICE.")

        sweeperService.debugUnblock(layer, state.userId)

    }


}
