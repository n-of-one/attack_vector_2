package org.n1.av2.run.terminal.inside

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class CommandViewService(
    private val connectionService: ConnectionService,
    private val nodeEntityService: NodeEntityService,
    private val commandServiceUtil: CommandServiceUtil,
) {

    fun process(runId: String, state: HackerStateRunning) {
        val node = nodeEntityService.getById(state.currentNodeId)

        val blockingIceLevel = commandServiceUtil.findBlockingIceLayer(node, runId)?.level ?: -1

        val lines = ArrayList<String>()


        lines.add("Node service layers:")
        node.layers.forEach { layer ->
            val blocked = (layer.level < blockingIceLevel)

            val hacked = if (layer is IceLayer && layer.hacked) " [mute]hacked[/]" else ""
            val iceSuffix = if (layer is IceLayer) " ICE" else ""
            if (blocked) {
                lines.add("[pri]${layer.level}[/] unknown (shielded by ICE)")
            }
            else {
                lines.add("[pri]${layer.level}[/] ${layer.name}${iceSuffix}${hacked}")
            }
        }

        connectionService.reply(ServerActions.SERVER_TERMINAL_RECEIVE, ConnectionService.TerminalReceive("main", lines.map { it }.toTypedArray()))
    }

}
