package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.NodeStatusService
import org.n1.av2.backend.service.run.ServiceStatusService


@org.springframework.stereotype.Service
class HackedUtil(
        val serviceStatusService: ServiceStatusService,
        val currentUser: CurrentUserService,
        val nodeStatusService: NodeStatusService,
        val stompService: StompService
) {

    data class IceHackedUpdate(val serviceId: String, val nodeId: String)
    data class NodeHacked(val nodeId: String, val delay: Int)

    fun iceHacked(serviceId: String, node: Node, runId: String, delay: Int) {


        val lastNonHackedIceServicel = findLastNonHackedIceService(node, runId)

        saveServiceStatusHacked(serviceId, runId)

        val update = IceHackedUpdate(serviceId, node.id)
        stompService.toRun(runId, ReduxActions.SERVER_SERVICE_HACKED, update)

        if (serviceId == lastNonHackedIceServicel) {
            nodeStatusService.createHackedStatus(node.id, runId)
            val nodeHackedUpdate = NodeHacked(node.id, delay)
            stompService.toRun(runId, ReduxActions.SERVER_NODE_HACKED, nodeHackedUpdate)
        }
    }

    fun nonIceHacked(serviceId: String, node: Node, runId: String) {
        saveServiceStatusHacked(serviceId, runId)
        val update = IceHackedUpdate(serviceId, node.id)
        stompService.toRun(runId, ReduxActions.SERVER_SERVICE_HACKED, update)
    }

    private fun saveServiceStatusHacked(serviceId: String, runId: String) {
        val layerStatus = serviceStatusService.getOrCreate(serviceId, runId)
        layerStatus.hackedBy.add(currentUser.userId)
        layerStatus.hacked = true
        serviceStatusService.save(layerStatus)
    }

    private fun findLastNonHackedIceService(node: Node, runId: String): String? {
        val iceServiceIds = node.services.filter {it.type.ice }. map { it.id }
        val serviceStatuses = serviceStatusService.getServicesStatus(runId, iceServiceIds)
        val hackedServiceIds = serviceStatuses.filter { it.hacked } .map { it.serviceId }
        val nonHackedIceServiceIds = iceServiceIds.subtract(hackedServiceIds)

        return if (nonHackedIceServiceIds.size == 1) {
            nonHackedIceServiceIds.first()
        }
        else {
            null
        }
    }




}