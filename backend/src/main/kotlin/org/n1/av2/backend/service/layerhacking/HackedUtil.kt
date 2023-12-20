package org.n1.av2.backend.service.layerhacking

import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.springframework.context.annotation.Lazy


@org.springframework.stereotype.Service
class HackedUtil(
    private val currentUser: CurrentUserService,
    private val nodeEntityService: NodeEntityService,
    private val stompService: StompService,
    @Lazy private val runService: RunService,
) {

    data class IceHackedUpdate(val layerId: String, val nodeId: String)
    data class NodeHacked(val nodeId: String, val delay: Int)

    fun iceHacked(layerId: String, delay: Int) {
        val node = nodeEntityService.findByLayerId(layerId)
        iceHacked(layerId, node, delay)
    }

    fun iceHacked(layerId: String, node: Node, delay: Int) {
        setIceLayerAsHacked(node, layerId)
        nodeEntityService.save(node)

        val update = IceHackedUpdate(layerId, node.id)
        stompService.toSite(node.siteId, ServerActions.SERVER_LAYER_HACKED, update) // to update scan status of site

        if (node.hacked) {
            runService.updateNodeStatusToHacked(node)

            val nodeHackedUpdate = NodeHacked(node.id, delay)
            stompService.toSite(node.siteId, ServerActions.SERVER_NODE_HACKED, nodeHackedUpdate)
        }
    }

    private fun setIceLayerAsHacked(node: Node, layerId: String) {
        val layer = node.getLayerById(layerId)
        if (layer !is IceLayer) error("Trying to hack non-ice layer")

        layer.hacked = true
    }
}