package org.n1.av2.run.terminal.inside

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.layer.Layer
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class InsideTerminalHelper(
    private val connectionService: ConnectionService,
    private val nodeEntityService: NodeEntityService,
) {

    fun verifyInside(hackerState: HackerStateRunning): Boolean {
        if (hackerState.activity != HackerActivity.INSIDE) {
            connectionService.replyTerminalReceive("[warn]You are outside[/] - First, start the attack with: [b]attack[/]")
            return false
        }
        return true
    }

    fun findBlockingIceLayer(node: Node, runId: String): Layer? {
        val iceLayers = node.layers.filterIsInstance<IceLayer>()
        return iceLayers.findLast { !it.hacked }
    }

    fun verifyCanAccessLayer(layerArgument: String, hackerState: HackerStateRunning, command: String): Layer? {
        requireNotNull(hackerState.currentNodeId)
        val node = nodeEntityService.getById(hackerState.currentNodeId)

        val level = layerArgument.toIntOrNull() ?: return reportLayerUnknown(node, layerArgument, command)
        if (level < 0 || level >= node.layers.size) return reportLayerUnknown(node, layerArgument, command)

        val blockingIceLayer = findBlockingIceLayer(node, hackerState.runId)
        if (blockingIceLayer != null && blockingIceLayer.level > level) return reportBlockingIce(blockingIceLayer)

        val layer = node.layers.find { it.level == level }!!
        return layer
    }

    private fun reportLayerUnknown(node: Node, layerInput: String, command: String): Layer? {
        val layerCount = node.layers.size
        if (layerCount == 1) {
            connectionService.replyTerminalReceive("[error]layer error[/] - Layer number [primary]${layerInput}[/] not found.",
                "This node has only one layer, the only valid option is: [b]${command} [primary]0[/].")
        } else {
            connectionService.replyTerminalReceive("[error]layer error[/] - Layer number [primary]${layerInput}[/] not found.",
                "This node has ${layerCount} layers, so use a number between [primary]0[/] and [primary]${layerCount - 1}[/].",
                "Use [b]view[/] to see the layers and their numbers.")
        }
        return null
    }

    private fun reportBlockingIce(blockingIceLayer: Layer): Layer? {
        connectionService.replyTerminalReceive("[warn b]blocked[/] - ICE (${blockingIceLayer.name}) blocks access to this layer. Hack the ICE first: [b]hack[/] [primary]${blockingIceLayer.level}")
        return null
    }

}
