package org.n1.av2.layer.ice

import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.SystemTaskRunner
import org.n1.av2.run.RunService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.statistics.IceHackState
import org.n1.av2.statistics.IceStatisticsService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service


@Service
class HackedUtil(
    private val nodeEntityService: NodeEntityService,
    private val connectionService: ConnectionService,
    private val systemTaskRunner: SystemTaskRunner,
    private val iceStatisticsService: IceStatisticsService,
    @Lazy private val runService: RunService,
) {

    data class IceHackedUpdate(val layerId: String, val nodeId: String)
    data class NodeHacked(val nodeId: String, val delay: Int)

    fun iceHacked(iceId: String, layerId: String, delayTicks: Int, iceHackState: IceHackState) {
        val node = nodeEntityService.findByLayerId(layerId)
        iceHacked(iceId, layerId, node, delayTicks, iceHackState)
    }

    fun iceHacked(iceId: String, layerId: String, node: Node, delayTicks: Int, iceHackState: IceHackState) {
        setIceLayerAsHacked(node, layerId)
        nodeEntityService.save(node)

        connectionService.toIce(iceId, ServerActions.SERVER_ICE_HACKED)

        val update = IceHackedUpdate(layerId, node.id)
        connectionService.toSite(node.siteId, ServerActions.SERVER_LAYER_HACKED, update) // to update scan status of site

        if (node.hacked) {
            val nodeHackedUpdate = NodeHacked(node.id, delayTicks)
            connectionService.toSite(node.siteId, ServerActions.SERVER_NODE_HACKED, nodeHackedUpdate)

            systemTaskRunner.queueInTicks("node hacked", mapOf("siteId" to node.siteId), delayTicks) {
                runService.updateNodeStatusToHacked(node)
            }
        }

        iceStatisticsService.finishHackingIce(iceId, iceHackState)
    }

    private fun setIceLayerAsHacked(node: Node, layerId: String) {
        val layer = node.getLayerById(layerId)
        if (layer !is IceLayer) error("Trying to hack non-ice layer")

        layer.hacked = true
    }
}
