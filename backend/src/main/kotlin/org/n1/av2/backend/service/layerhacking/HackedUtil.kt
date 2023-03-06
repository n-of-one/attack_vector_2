package org.n1.av2.backend.service.layerhacking

import org.n1.av2.backend.entity.run.LayerStatusEntityService
import org.n1.av2.backend.entity.run.NodeStatusEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.util.nodeIdFromLayerId


@org.springframework.stereotype.Service
class HackedUtil(
    val layerStatusEntityService: LayerStatusEntityService,
    val currentUser: CurrentUserService,
    val nodeEntityService: NodeEntityService,
    val nodeStatusEntityService: NodeStatusEntityService,
    val stompService: StompService
) {

    data class IceHackedUpdate(val layerId: String, val nodeId: String)
    data class NodeHacked(val nodeId: String, val delay: Int)

    fun iceHacked(layerId: String, runId: String, delay: Int) {
        val nodeId = nodeIdFromLayerId(layerId)
        val node = nodeEntityService.getById(nodeId)
        iceHacked(layerId, node, runId, delay)
    }

    fun iceHacked(layerId: String, node: Node, runId: String, delay: Int) {
        val lastNonHackedIceLayerId = findLastNonHackedIceLayerId(node, runId)
        saveLayerStatusHacked(layerId, runId)

        val update = IceHackedUpdate(layerId, node.id)
        stompService.toRun(runId, ServerActions.SERVER_LAYER_HACKED, update)

        if (layerId == lastNonHackedIceLayerId) {
            nodeStatusEntityService.createHackedStatus(node.id, runId)
            val nodeHackedUpdate = NodeHacked(node.id, delay)
            stompService.toRun(runId, ServerActions.SERVER_NODE_HACKED, nodeHackedUpdate)
        }
    }

    fun nonIceHacked(layerId: String, node: Node, runId: String) {
        saveLayerStatusHacked(layerId, runId)
        val update = IceHackedUpdate(layerId, node.id)
        stompService.toRun(runId, ServerActions.SERVER_LAYER_HACKED, update)
    }

    private fun saveLayerStatusHacked(layerId: String, runId: String) {
        val layerStatus = layerStatusEntityService.getOrCreate(layerId, runId)
        layerStatus.hackedBy.add(currentUser.userId)
        layerStatus.hacked = true
        layerStatusEntityService.save(layerStatus)
    }

    private fun findLastNonHackedIceLayerId(node: Node, runId: String): String? {
        val iceLayerIds = node.layers.filter {it.type.ice }. map { it.id }
        val layerStatuses = layerStatusEntityService.getLayerStatuses(iceLayerIds, runId)
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