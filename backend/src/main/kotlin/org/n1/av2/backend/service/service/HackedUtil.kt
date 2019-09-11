package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.LayerStatusService
import org.n1.av2.backend.service.run.NodeStatusService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.util.nodeIdFromServiceId


@org.springframework.stereotype.Service
class HackedUtil(
        val layerStatusService: LayerStatusService,
        val currentUser: CurrentUserService,
        val nodeService: NodeService,
        val nodeStatusService: NodeStatusService,
        val stompService: StompService
) {

    data class IceHackedUpdate(val layerId: String, val nodeId: String)
    data class NodeHacked(val nodeId: String, val delay: Int)

    fun iceHacked(layerId: String, runId: String, delay: Int) {
        val nodeId = nodeIdFromServiceId(layerId)
        val node = nodeService.getById(nodeId)
        iceHacked(layerId, node, runId, delay)
    }

    fun iceHacked(layerId: String, node: Node, runId: String, delay: Int) {
        val lastNonHackedIceLayerId = findLastNonHackedIceLayerId(node, runId)
        saveLayerStatusHacked(layerId, runId)

        val update = IceHackedUpdate(layerId, node.id)
        stompService.toRun(runId, ReduxActions.SERVER_LAYER_HACKED, update)

        if (layerId == lastNonHackedIceLayerId) {
            nodeStatusService.createHackedStatus(node.id, runId)
            val nodeHackedUpdate = NodeHacked(node.id, delay)
            stompService.toRun(runId, ReduxActions.SERVER_NODE_HACKED, nodeHackedUpdate)
        }
    }

    fun nonIceHacked(layerId: String, node: Node, runId: String) {
        saveLayerStatusHacked(layerId, runId)
        val update = IceHackedUpdate(layerId, node.id)
        stompService.toRun(runId, ReduxActions.SERVER_LAYER_HACKED, update)
    }

    private fun saveLayerStatusHacked(layerId: String, runId: String) {
        val layerStatus = layerStatusService.getOrCreate(layerId, runId)
        layerStatus.hackedBy.add(currentUser.userId)
        layerStatus.hacked = true
        layerStatusService.save(layerStatus)
    }

    private fun findLastNonHackedIceLayerId(node: Node, runId: String): String? {
        val iceLayerIds = node.layers.filter {it.type.ice }. map { it.id }
        val layerStatuses = layerStatusService.getServicesStatus(runId, iceLayerIds)
        val hackedLayerIds = layerStatuses.filter { it.hacked } .map { it.layerId }
        val nonHackedIceLayerIds = iceLayerIds.subtract(hackedLayerIds)

        return if (nonHackedIceLayerIds.size == 1) {
            nonHackedIceLayerIds.first()
        }
        else {
            null
        }
    }




}