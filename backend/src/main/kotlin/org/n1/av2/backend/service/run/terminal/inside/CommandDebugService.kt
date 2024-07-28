package org.n1.av2.backend.service.run.terminal.inside

import org.n1.av2.backend.entity.run.HackerStateRunning
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.ice.SweeperIceLayer
import org.n1.av2.backend.service.layerhacking.ice.sweeper.SweeperService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class CommandDebugService(
    private val stompService: StompService,
    private val sweeperService: SweeperService,
    private val nodeEntityService: NodeEntityService,

    ) {

    fun processSweepUnblock(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (tokens.size != 2 ) {
            return stompService.replyTerminalReceive("Missing layer level.")
        }
        val level = tokens[1].toIntOrNull() ?: return stompService.replyTerminalReceive("Layer ${tokens[1]} is not a valid layer.")
        val node = nodeEntityService.getById(state.currentNodeId)
        val layer = node.layers.find { it.level == level }!!

        if (layer !is SweeperIceLayer) return stompService.replyTerminalReceive("Layer is not sweeper ICE.")

        sweeperService.debugUnblock(layer, state.userId)

    }


}