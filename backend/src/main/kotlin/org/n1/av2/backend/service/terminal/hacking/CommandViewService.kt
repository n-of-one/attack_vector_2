package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.model.db.run.HackerStateRunning
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.LayerStatusService
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service

@Service
class CommandViewService(
        private val stompService: StompService,
        private val nodeService: NodeService,
        private val commandServiceUtil: CommandServiceUtil,
        private val layerStatusService: LayerStatusService
) {

    fun process(runId: String, state: HackerStateRunning) {
        val node = nodeService.getById(state.currentNodeId)

        val blockingIceLevel = commandServiceUtil.findBlockingIceLayer(node, runId)?.level ?: -1

        val lines = ArrayList<String>()

        val layerStatuses = layerStatusService.getLayerStatuses(node, runId)
        val hackedLayerIds = layerStatuses.filter { it.hacked }.map { it.layerId }

        lines.add("Node service layers:")
        node.layers.forEach { layer ->
            val blocked = if (layer.level < blockingIceLevel) "* " else ""
            val hacked = if (hackedLayerIds.contains(layer.id)) " [mute]hacked[/]" else ""
            val iceSuffix = if (layer.type.ice) " ICE" else ""
            lines.add("${blocked}[pri]${layer.level}[/] ${layer.name}${iceSuffix}${hacked}")
        }

        stompService.terminalReceiveCurrentUser(lines)
    }

}