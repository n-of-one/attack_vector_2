package org.n1.av2.layer.ice

import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.run.RunService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.context.annotation.Lazy


@org.springframework.stereotype.Service
class HackedUtil(
    private val nodeEntityService: NodeEntityService,
    private val connectionService: ConnectionService,
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
        connectionService.toSite(node.siteId, ServerActions.SERVER_LAYER_HACKED, update) // to update scan status of site

        if (node.hacked) {
            runService.updateNodeStatusToHacked(node)

            val nodeHackedUpdate = NodeHacked(node.id, delay)
            connectionService.toSite(node.siteId, ServerActions.SERVER_NODE_HACKED, nodeHackedUpdate)
        }
    }

    private fun setIceLayerAsHacked(node: Node, layerId: String) {
        val layer = node.getLayerById(layerId)
        if (layer !is IceLayer) error("Trying to hack non-ice layer")

        layer.hacked = true
    }
}
