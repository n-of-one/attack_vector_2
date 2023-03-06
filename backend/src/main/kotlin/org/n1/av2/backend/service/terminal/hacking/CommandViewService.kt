package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.entity.run.HackerStateRunning
import org.n1.av2.backend.entity.run.LayerStatusEntityService
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class CommandViewService(
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val commandServiceUtil: CommandServiceUtil,
    private val layerStatusEntityService: LayerStatusEntityService
) {

    fun process(runId: String, state: HackerStateRunning) {
        val node = nodeEntityService.getById(state.currentNodeId)

        val blockingIceLevel = commandServiceUtil.findBlockingIceLayer(node, runId)?.level ?: -1

        val lines = ArrayList<String>()

        val layerStatuses = layerStatusEntityService.getLayerStatuses(node, runId)
        val hackedLayerIds = layerStatuses.filter { it.hacked }.map { it.layerId }

        lines.add("Node service layers:")
        node.layers.forEach { layer ->
            val blocked = if (layer.level < blockingIceLevel) "* " else ""
            val hacked = if (hackedLayerIds.contains(layer.id)) " [mute]hacked[/]" else ""
            val iceSuffix = if (layer.type.ice) " ICE" else ""
            lines.add("${blocked}[pri]${layer.level}[/] ${layer.name}${iceSuffix}${hacked}")
        }

        stompService.reply(ServerActions.SERVER_TERMINAL_RECEIVE, StompService.TerminalReceive("main", lines.map { it }.toTypedArray()))
    }

}