package org.n1.av2.backend.service.run.inside.command

import org.n1.av2.backend.entity.run.HackerStateRunning
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class CommandViewService(
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val commandServiceUtil: CommandServiceUtil,
) {

    fun process(runId: String, state: HackerStateRunning) {
        val node = nodeEntityService.getById(state.currentNodeId)

        val blockingIceLevel = commandServiceUtil.findBlockingIceLayer(node, runId)?.level ?: -1

        val lines = ArrayList<String>()


        lines.add("Node service layers:")
        node.layers.forEach { layer ->
            val blocked = if (layer.level < blockingIceLevel) "* " else ""
            val hacked = if (layer is IceLayer && layer.hacked) " [mute]hacked[/]" else ""
            val iceSuffix = if (layer is IceLayer) " ICE" else ""
            lines.add("${blocked}[pri]${layer.level}[/] ${layer.name}${iceSuffix}${hacked}")
        }

        stompService.reply(ServerActions.SERVER_TERMINAL_RECEIVE, StompService.TerminalReceive("main", lines.map { it }.toTypedArray()))
    }

}