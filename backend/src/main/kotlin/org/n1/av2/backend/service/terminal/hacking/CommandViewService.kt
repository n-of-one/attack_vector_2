package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.HackerPositionService
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service

@Service
class CommandViewService(
        private val stompService: StompService,
        private val nodeService: NodeService,
        private val hackerPositionService: HackerPositionService

        ) {

    fun process(runId: String) {
        val position = hackerPositionService.get()
        val node = nodeService.getById(position.currentNodeId)

        val blockingIceLayer = findBlockingIceLayer(node) ?: -1

        val lines = ArrayList<String>()

        lines.add("Node service layers:")
        node.services.forEach { service ->
            val blocked = if (service.layer < blockingIceLayer) "* " else ""
            val hacked = if (service.hacked) " [mute]hacked[/]" else ""
            val iceSuffix = if (service.type.ice) " ICE" else ""
            lines.add("${blocked}[pri]${service.layer}[/] ${service.name}${iceSuffix}${hacked}")
        }

        stompService.terminalReceive(lines)
    }
}