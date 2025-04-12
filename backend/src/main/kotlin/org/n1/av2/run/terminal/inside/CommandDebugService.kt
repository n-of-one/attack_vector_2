package org.n1.av2.run.terminal.inside

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.layer.ice.sweeper.SweeperIceLayer
import org.n1.av2.layer.ice.sweeper.SweeperService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.run.terminal.generic.DevCommandHelper
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class CommandDebugService(
    private val connectionService: ConnectionService,
    private val sweeperService: SweeperService,
    private val nodeEntityService: NodeEntityService,
    private val devCommandHelper: DevCommandHelper,
    private val insideTerminalHelper: InsideTerminalHelper,
) {

    fun processSweepUnblock(arguments: List<String>, hackerState: HackerStateRunning) {
        if (!devCommandHelper.checkDevModeEnabled()) return
        if (!insideTerminalHelper.verifyInside(hackerState)) return
        requireNotNull(hackerState.currentNodeId)

        if (arguments.isEmpty()) {
            return connectionService.replyTerminalReceive("Missing layer level.")
        }
        val level = arguments.first().toIntOrNull() ?: return connectionService.replyTerminalReceive("Layer ${arguments.first()} is not a valid layer.")
        val node = nodeEntityService.getById(hackerState.currentNodeId)
        val layer = node.layers.find { it.level == level }!!

        if (layer !is SweeperIceLayer) return connectionService.replyTerminalReceive("Layer is not sweeper ICE.")

        sweeperService.debugUnblock(layer, hackerState.userId)
    }
}
