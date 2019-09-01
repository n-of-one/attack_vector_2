package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.repo.ServiceStatusRepo
import org.springframework.stereotype.Service

@Service
class CommandServiceUtil(
        private val serviceStatusRepo: ServiceStatusRepo
) {
    fun findBlockingIceLayer(node: Node, runId: String): Int? {

        if (node.services.none { it.type.ice }) {
            return null
        }

        val iceServiceIds = node.services.filter {it.type.ice }. map { it.id }
        val serviceStatuses = serviceStatusRepo.findByRunIdAndServiceIdIn(runId, iceServiceIds)
        val hackedServiceIds = serviceStatuses.filter { it.hacked } .map { it.serviceId }
        val nonHackedIceServiceIds = iceServiceIds.filter { !hackedServiceIds.contains(it) }
        if (nonHackedIceServiceIds.isEmpty()) {
            return null
        }
        val upperNonHackedIceServiceId = nonHackedIceServiceIds.last()
        val upperNonHackedIceService = node.services.find {it.id == upperNonHackedIceServiceId}!!
        return upperNonHackedIceService.layer
    }
}