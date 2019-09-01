package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.repo.ServiceStatusRepo
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.HackerPositionService
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service

@Service
class CommandViewService(
        private val stompService: StompService,
        private val nodeService: NodeService,
        private val hackerPositionService: HackerPositionService,
        private val commandServiceUtil: CommandServiceUtil,
        private val serviceStatusRepo: ServiceStatusRepo
) {

    fun process(runId: String) {
        val position = hackerPositionService.retrieveForCurrentUser()
        val node = nodeService.getById(position.currentNodeId)

        val blockingIceLayer = commandServiceUtil.findBlockingIceLayer(node, runId) ?: -1

        val lines = ArrayList<String>()

        val serviceStatuses = serviceStatusRepo.findByRunIdAndServiceIdIn(runId, node.services.map { it.id })
        val hackedServiceIds = serviceStatuses.filter { it.hacked }.map { it.serviceId }

        lines.add("Node service layers:")
        node.services.forEach { service ->
            val blocked = if (service.layer < blockingIceLayer) "* " else ""
            val hacked = if (hackedServiceIds.contains(service.id)) " [mute]hacked[/]" else ""
            val iceSuffix = if (service.type.ice) " ICE" else ""
            lines.add("${blocked}[pri]${service.layer}[/] ${service.name}${iceSuffix}${hacked}")
        }

        stompService.terminalReceive(lines)
    }
}